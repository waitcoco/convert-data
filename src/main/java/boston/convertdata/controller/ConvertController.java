package boston.convertdata.controller;

import boston.convertdata.model.elasticsearch.VideoInfo;
import boston.convertdata.repository.EsUploader;
import boston.convertdata.utils.GsonInstances;
import boston.convertdata.repository.VideoInfoGetter;
import boston.convertdata.model.elasticsearch.*;
import boston.convertdata.model.structured.*;
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
    public void convert(@RequestBody Video video) throws Exception {
        val pipelineVideo = videoInfoGetter.getVideo(video.getVideoInfo().getVideoId());
        val pipelineCamera = videoInfoGetter.getCamera(pipelineVideo.getCameraId());

        val records = new ArrayList<IndexRecord>();

        val videoInfo = new VideoInfo();
        videoInfo.setVideoId(video.getVideoInfo().getVideoId());
        videoInfo.setStartTime(pipelineVideo.getStartTime());

        val cameraPosition = new CameraPosition();
        cameraPosition.setLat(pipelineCamera.getPositionLat());
        cameraPosition.setLon(pipelineCamera.getPositionLon());
        val cameraInfo = new CameraInfo();
        cameraInfo.setCameraId(pipelineCamera.getId());
        cameraInfo.setCameraName(pipelineCamera.getName());
        cameraInfo.setPosition(cameraPosition);

        if (video.getFramesInfo() != null) {
            for (Frame frame : video.getFramesInfo()) {
                val frameInfo = new FrameInfo();
                frameInfo.setRelativeTime(frame.getFrameInfo().get(0).getRelativeTime());
                frameInfo.setAbsoluteTime(getAbsoluteTime(videoInfo.getStartTime(), frameInfo.getRelativeTime()));

                if (frame.getCars() != null) {
                    for (boston.convertdata.model.structured.Car car : frame.getCars()) {
                        records.add(new IndexRecord(car, videoInfo, cameraInfo, frameInfo));
                    }
                }

                if (frame.getPeople() != null) {
                    for (Person person : frame.getPeople()) {
                        records.add(new IndexRecord(person, videoInfo, cameraInfo, frameInfo));
                    }
                }
            }
        }

        try (val uploader = new EsUploader(restHighLevelClient, indexName, type, batchSize)) {
//            if (uploader.indexExists()) {
//                uploader.deleteIndex();
//            }
            if (!uploader.indexExists()) {
                uploader.createIndex("camera_info.position", "type=geo_point");
            }
            for (Object record : records) {
                String jsonRepresentation = GsonInstances.ELASTICSEARCH.toJson(record);
                uploader.addJsonDocument(jsonRepresentation);
                System.out.println(jsonRepresentation + "\n");
            }
            uploader.flush();
        }
    }

    private static Instant getAbsoluteTime(Instant startTime, LocalTime relativeTime){
        return startTime.plusNanos(relativeTime.toNanoOfDay());
    }
}
