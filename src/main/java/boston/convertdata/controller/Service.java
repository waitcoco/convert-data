package boston.convertdata.controller;

import boston.convertdata.EsUploader;
import boston.convertdata.HiveHelper;
import boston.convertdata.model.Video;
import boston.convertdata.model.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.val;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;

@RestController
@Data
public class Service {
    private static final String elasticSearchUrl = "http://47.104.22.92:9200";
    private static final String indexName = "video_data_v2_test";

    @PostMapping
    public ResponseEntity postController(
            @RequestBody Video video) throws Exception {
            ConvertData(video);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private static void ConvertData(Video video) throws Exception {
        val hiveController = new HiveHelper();
        val result = new ArrayList<>();
        for(int i = 0; i < video.getFramesInfo().size(); i++){
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setVideoId(video.getVideoInfo().getVideoId());
            val startTime = "2018-01-01T12:00:00Z";
            videoInfo.setStartTime(startTime);
            val relativeTime = video.getFramesInfo().get(i).getFrameInfo().get(0).getRelativeTime();
            val frameInfo = new FrameInfo();
            frameInfo.setRelativeTime(relativeTime);
            val absoluteTime = absoluteTime(startTime,relativeTime);
            frameInfo.setAbsoluteTim(absoluteTime);
            for(int j = 0; j < video.getFramesInfo().get(i).getCars().size();j++){
                CameraPosition cameraPosition = new CameraPosition();
                cameraPosition.setLat(51.4);
                cameraPosition.setLon(15.4);
                CameraInfo cameraInfo = new CameraInfo();
                cameraInfo.setCameraId("c000001");
                cameraInfo.setCameraName("朝阳区新东路01");
                cameraInfo.setPosition(cameraPosition);
                Car car = video.getFramesInfo().get(i).getCars().get(j);
                CarTask carTask = new CarTask(car, videoInfo,cameraInfo,frameInfo);
                result.add(carTask);
//                String sql = "insert into object_data select named_struct（'carId','"+carTask.car.carId+"','make','"+carTask.car.make
//                        +"','model','"+carTask.car.model+"','carColor',"+carTask.car.carColor+"','carLeftbottom',"+carTask.car.car_leftbottom
//                        +"','licensePlateNo',"+carTask.car.licensePlateNo+"','carRightbottom',"+carTask.car.car_rightbottom+
//                        "','carRighttop',"+carTask.car.car_righttop+"','licensePlateColor',"+carTask.car.license_plate_color+
//                        "','licensePlateLeftbottom',"+carTask.car.license_plate_leftbottom+"','"
//                hiveController.executedNoquery(sql);
            }
        }
        for(int i = 0; i < video.getFramesInfo().size(); i++){
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setVideoId(video.getVideoInfo().getVideoId());
            val startTime = "2018-01-01T12:00:00Z";
            videoInfo.setStartTime(startTime);
            val relativeTime = video.getFramesInfo().get(i).getFrameInfo().get(0).getRelativeTime();
            val frame_info = new FrameInfo();
            frame_info.setRelativeTime(relativeTime);
            val absoluteTime = absoluteTime(startTime,relativeTime);
            frame_info.setAbsoluteTim(absoluteTime);
            for(int j = 0; j < video.getFramesInfo().get(i).getPeople().size();j++){
                CameraPosition cameraPosition = new CameraPosition();
                cameraPosition.setLat(51.4);
                cameraPosition.setLon(15.4);
                CameraInfo cameraInfo = new CameraInfo();
                cameraInfo.setCameraId("c000001");
                cameraInfo.setCameraName("朝阳区新东路01");
                cameraInfo.setPosition(cameraPosition);
                Person person = video.getFramesInfo().get(i).getPeople().get(j);
                PersonTask personTask = new PersonTask(person, videoInfo,cameraInfo,frame_info);
                result.add(personTask);
//                String sql = "insert into object_data values（"+ personTask.video_information+","+personTask.camera_info+","
//                        +personTask.person+",null,"+personTask.frame_info+")";
//                hiveController.executedNoquery(sql);
            }
        }

        try (val uploader = new EsUploader(elasticSearchUrl, indexName, 1000)) {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            for (int i = 0; i < result.size(); i++) {
                String jsonRepresentation = gson.toJson(result.get(i));
                uploader.addJsonDocument(jsonRepresentation);
                System.out.println(jsonRepresentation + "\n");
            }
            uploader.flush();
        }
    }
    public static String absoluteTime(String startTime, String relativeTime){
        DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_DATE_TIME;
        TemporalAccessor accessor = timeFormatter.parse(startTime);
        Instant videoStartTime = Instant.from(accessor);
        val videoRelativeTime = LocalTime.parse(relativeTime);
        val absoluteTime = videoStartTime.plusNanos(videoRelativeTime.toNanoOfDay());
        return absoluteTime.toString();
    }
}
