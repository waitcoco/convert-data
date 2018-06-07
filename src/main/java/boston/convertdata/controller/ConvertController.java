package boston.convertdata.controller;

import boston.convertdata.model.structured.Frame;
import boston.convertdata.repository.EsUploader;
import boston.convertdata.utils.GsonInstances;
import boston.convertdata.repository.VideoInfoGetter;
import boston.convertdata.model.elasticsearch.*;
import lombok.val;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;

@RestController
public class ConvertController {

    private final RestHighLevelClient restHighLevelClient;
    private final VideoInfoGetter videoInfoGetter;
    private final String indexName;
    private final String type;
    private final int batchSize;

    @Autowired
    public ConvertController(RestHighLevelClient restHighLevelClient,
                             VideoInfoGetter videoInfoGetter,
                             @Value("${elasticsearch.indexName}") String indexName,
                             @Value("${elasticsearch.type:doc}") String type,
                             @Value("${elasticsearch.batchSize:1000}") int batchSize) {
        this.restHighLevelClient = restHighLevelClient;
        this.videoInfoGetter = videoInfoGetter;
        this.indexName = indexName;
        this.type = type;
        this.batchSize = batchSize;
    }

    @PostMapping
    public void convert(@RequestBody boston.convertdata.model.structured.Video video) throws Exception {
        val pipelineVideo = videoInfoGetter.getVideo(video.getVideoId());
        val pipelineCamera = videoInfoGetter.getCamera(pipelineVideo.getCameraId());

        val records = new ArrayList<Segment>();

        val videoInfo = new VideoInfo();
        videoInfo.setVideoId(video.getVideoId());
        videoInfo.setStartTime(pipelineVideo.getStartTime());
        videoInfo.setVideoUrl(pipelineVideo.getPlaybackUrl());

        val cameraPosition = new Position();
        cameraPosition.setLat(pipelineCamera.getPositionLat());
        cameraPosition.setLon(pipelineCamera.getPositionLon());
        val cameraInfo = new CameraInfo();
        cameraInfo.setCameraId(pipelineCamera.getId());
        cameraInfo.setCameraName(pipelineCamera.getName());
        cameraInfo.setPosition(cameraPosition);

        if (video.getSegmentsInfo() != null) {
            for (boston.convertdata.model.structured.Segment segment : video.getSegmentsInfo()) {
                if (segment.getFramesInfo() == null || segment.getFramesInfo().isEmpty()) {
                    continue;
                }

                val esSegment = new Segment();
                esSegment.setSegmentId(segment.getSegmentId());
                esSegment.setCameraInfo(cameraInfo);
                esSegment.setVideoInfo(videoInfo);
                esSegment.setCar(segment.getCar());
                esSegment.setPerson(segment.getPerson());
                esSegment.setFramesInfo(segment.getFramesInfo());

                val firstFrame = segment.getFramesInfo().stream().min(Comparator.comparing(Frame::getRelativeTime)).get();
                val lastFrame = segment.getFramesInfo().stream().max(Comparator.comparing(Frame::getRelativeTime)).get();
                esSegment.setRelativeStartTime(firstFrame.getRelativeTime());
                esSegment.setRelativeEndTime(lastFrame.getRelativeTime());
                esSegment.setStartTime(getAbsoluteTime(videoInfo.getStartTime(), esSegment.getRelativeStartTime()));
                esSegment.setEndTime(getAbsoluteTime(videoInfo.getStartTime(), esSegment.getRelativeEndTime()));

                records.add(esSegment);
            }
        }

        try (val uploader = new EsUploader(restHighLevelClient, indexName, type, batchSize)) {
//            if (uploader.indexExists()) {
//                uploader.deleteIndex();
//            }
            if (!uploader.indexExists()) {
                uploader.createIndex("camera_info.position", "type=geo_point");
            }
            for (val record : records) {
                String jsonRepresentation = GsonInstances.ELASTICSEARCH.toJson(record);
                uploader.addJsonDocument(record.getSegmentId(), jsonRepresentation);
            }
            uploader.flush();
        }
    }

    private static Instant getAbsoluteTime(Instant startTime, LocalTime relativeTime){
        return startTime.plusNanos(relativeTime.toNanoOfDay());
    }
}
