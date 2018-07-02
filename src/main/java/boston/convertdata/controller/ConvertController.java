package boston.convertdata.controller;

import boston.convertdata.model.structured.Frame;
import boston.convertdata.repository.EsUploader;
import boston.convertdata.repository.ImageUploader;
import boston.convertdata.repository.ImageVectorRepository;
import boston.convertdata.utils.GsonInstances;
import boston.convertdata.repository.VideoInfoGetter;
import boston.convertdata.model.elasticsearch.*;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.util.*;

@Log4j2
@RestController
public class ConvertController {

    private final EsUploader uploader;
    private final VideoInfoGetter videoInfoGetter;
    private final ImageUploader imageUploader;
    private final ImageVectorRepository imageVectorRepository;

    @Autowired
    public ConvertController(EsUploader uploader,
                             VideoInfoGetter videoInfoGetter,
                             ImageUploader imageUploader,
                             ImageVectorRepository imageVectorRepository) {
        this.uploader = uploader;
        this.videoInfoGetter = videoInfoGetter;
        this.imageUploader = imageUploader;
        this.imageVectorRepository = imageVectorRepository;
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
            Map<String, double[]> vectorMap = new HashMap<>();
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

                // 上传图片, 获得objectImgUrl
                String filename = "segment-" + segment.getSegmentId() + ".jpeg";
                val objectImageBytes = Base64.getDecoder().decode(segment.getObjectImg());
                String objectImgUrl = imageUploader.uploadImage(objectImageBytes, filename);
                esSegment.setObjectImgUrl(objectImgUrl);

                val firstFrame = segment.getFramesInfo().stream().min(Comparator.comparing(Frame::getRelativeTime)).get();
                val lastFrame = segment.getFramesInfo().stream().max(Comparator.comparing(Frame::getRelativeTime)).get();
                esSegment.setRelativeStartTime(firstFrame.getRelativeTime());
                esSegment.setRelativeEndTime(lastFrame.getRelativeTime());
                esSegment.setStartTime(getAbsoluteTime(videoInfo.getStartTime(), esSegment.getRelativeStartTime()));
                esSegment.setEndTime(getAbsoluteTime(videoInfo.getStartTime(), esSegment.getRelativeEndTime()));

                records.add(esSegment);

                // 得到缩略图向量
                vectorMap.put(segment.getSegmentId(), imageVectorRepository.getImageVector(filename, objectImageBytes));
            }
            // 一个video的所有segment缩略图, 写入文件
            imageVectorRepository.saveVectorToFile(vectorMap, video.getVideoId());
        }

        if (!uploader.indexExists()) {
            uploader.createIndex("camera_info.position", "type=geo_point");
        }

        try (val session = uploader.newSession()) {
            for (val record : records) {
                String jsonRepresentation = GsonInstances.ELASTICSEARCH.toJson(record);
                session.addJsonDocument(record.getSegmentId(), jsonRepresentation);
            }
        }
    }

    private static Instant getAbsoluteTime(Instant startTime, LocalTime relativeTime) {
        return startTime.plusNanos(relativeTime.toNanoOfDay());
    }

    @PostMapping("deleteIndex")
    public void deleteIndex() throws IOException {
        uploader.deleteIndex();
    }

}
