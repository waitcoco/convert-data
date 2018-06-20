package boston.convertdata.controller;

import boston.convertdata.model.structured.Frame;
import boston.convertdata.model.structured.Video;
import boston.convertdata.repository.EsUploader;
import boston.convertdata.repository.ImageUploader;
import boston.convertdata.repository.ImageVectorRepository;
import boston.convertdata.utils.GsonInstances;
import boston.convertdata.repository.VideoInfoGetter;
import boston.convertdata.model.elasticsearch.*;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;

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
            File vectorFile = null;
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
                String objectImgUrl = imageUploader.uploadImage(segment.getObjectImg(), "segment-" + segment.getSegmentId() + ".jpeg");
                esSegment.setObjectImgUrl(objectImgUrl);

                val firstFrame = segment.getFramesInfo().stream().min(Comparator.comparing(Frame::getRelativeTime)).get();
                val lastFrame = segment.getFramesInfo().stream().max(Comparator.comparing(Frame::getRelativeTime)).get();
                esSegment.setRelativeStartTime(firstFrame.getRelativeTime());
                esSegment.setRelativeEndTime(lastFrame.getRelativeTime());
                esSegment.setStartTime(getAbsoluteTime(videoInfo.getStartTime(), esSegment.getRelativeStartTime()));
                esSegment.setEndTime(getAbsoluteTime(videoInfo.getStartTime(), esSegment.getRelativeEndTime()));

                records.add(esSegment);

                // 一个segment获取缩略图向量, 并写入所属video的文件
                vectorFile = processVector(segment, video.getVideoId());
            }

            // 一个video的所有segment写完后, 将文件名最后.temp去掉
            int end = vectorFile.getAbsolutePath().length() - 5;
            vectorFile.renameTo(new File(vectorFile.getAbsolutePath().substring(0, end))); // 移除.temp
        }

        if (!uploader.indexExists()) {
            uploader.createIndex("camera_info.position", "type=geo_point");
        }
        for (val record : records) {
            String jsonRepresentation = GsonInstances.ELASTICSEARCH.toJson(record);
            uploader.addJsonDocument(record.getSegmentId(), jsonRepresentation);
        }
        uploader.flush();
    }

    private static Instant getAbsoluteTime(Instant startTime, LocalTime relativeTime) {
        return startTime.plusNanos(relativeTime.toNanoOfDay());
    }

    @PostMapping("deleteIndex")
    public void deleteIndex() throws IOException {
        uploader.deleteIndex();
    }

    private File processVector(boston.convertdata.model.structured.Segment segment, String videoId) throws IOException {
        String objectType = segment.getCar() != null ? "car" : "person";
        // 获得一张缩略图向量
        double[] vector = imageVectorRepository.getImageVector(objectType, "segment-" + segment.getSegmentId() + ".jpeg", segment.getObjectImg());
        // 将该向量写入文件
        return imageVectorRepository.saveVectorForOneImage(vector, videoId + ".text.temp", segment.getSegmentId());
    }

}
