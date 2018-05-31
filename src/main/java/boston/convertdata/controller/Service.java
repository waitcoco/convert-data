package boston.convertdata.controller;

import boston.convertdata.HiveHelper;
import boston.convertdata.model.Video;
import boston.convertdata.model.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.val;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;

@RestController

public class Service {
    @PostMapping
    public ResponseEntity postController(
            @RequestBody Video video) {
            ConvertData(video);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    public static void ConvertData(Video video){
        val hiveController = new HiveHelper();
        val result = new ArrayList<>();
        for(int i = 0; i < video.getFramesInfo().size(); i++){
            VideoInformation videoInformation = new VideoInformation();
            videoInformation.setVideoId(video.getVideoInfo().getVideoId());
            val startTime = "2018-01-01T12:00:00Z";
            videoInformation.setStartTime(startTime);
            val relativeTime = video.getFramesInfo().get(i).getFrameInfo().get(0).getRelativeTime();
            val frame_info = new FrameInfo();
            frame_info.setRelativeTime(relativeTime);
            val absoluteTime = absoluteTime(startTime,relativeTime);
            frame_info.setAbsoluteTim(absoluteTime);
            for(int j = 0; j < video.getFramesInfo().get(i).getCars().size();j++){
                CameraPosition cameraPosition = new CameraPosition();
                cameraPosition.setLat("51.4");
                cameraPosition.setLon("15.4");
                CameraInfo cameraInfo = new CameraInfo();
                cameraInfo.setCameraId("c000001");
                cameraInfo.setCameraName("朝阳区新东路01");
                cameraInfo.setPosition(cameraPosition);
                Car car = video.getFramesInfo().get(i).getCars().get(j);
                CarTask carTask = new CarTask(car,videoInformation,cameraInfo,frame_info);
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
            VideoInformation videoInformation = new VideoInformation();
            videoInformation.setVideoId(video.getVideoInfo().getVideoId());
            val startTime = "2018-01-01T12:00:00Z";
            videoInformation.setStartTime(startTime);
            val relativeTime = video.getFramesInfo().get(i).getFrameInfo().get(0).getRelativeTime();
            val frame_info = new FrameInfo();
            frame_info.setRelativeTime(relativeTime);
            val absoluteTime = absoluteTime(startTime,relativeTime);
            frame_info.setAbsoluteTim(absoluteTime);
            for(int j = 0; j < video.getFramesInfo().get(i).getPeople().size();j++){
                CameraPosition cameraPosition = new CameraPosition();
                cameraPosition.setLat("51.4");
                cameraPosition.setLon("15.4");
                CameraInfo cameraInfo = new CameraInfo();
                cameraInfo.setCameraId("c000001");
                cameraInfo.setCameraName("朝阳区新东路01");
                cameraInfo.setPosition(cameraPosition);
                Person person = video.getFramesInfo().get(i).getPeople().get(j);
                PersonTask personTask = new PersonTask(person,videoInformation,cameraInfo,frame_info);
                result.add(personTask);
//                String sql = "insert into object_data values（"+ personTask.video_information+","+personTask.camera_info+","
//                        +personTask.person+",null,"+personTask.frame_info+")";
//                hiveController.executedNoquery(sql);
            }
        }
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        for(int i = 0; i < result.size(); i++){
            String jsonRepresentation = gson.toJson(result.get(i));
            System.out.println(jsonRepresentation+"\n");
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
