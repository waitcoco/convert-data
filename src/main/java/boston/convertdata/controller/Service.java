package boston.convertdata.controller;

import boston.convertdata.EsUploader;
import boston.convertdata.HiveHelper;
import boston.convertdata.VideoInfoGetter;
import boston.convertdata.model.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;

@RestController
public class Service {
    private static final String ELASTIC_SEARCH_URL = "http://47.104.22.92:9200";
    private static final String INDEX_NAME = "video_data_v2_test";
    private static final String TYPE = "doc";
    private static final int BATCH_SIZE = 1000;

    @PostMapping
    public ResponseEntity postController(
            @RequestBody Video video) throws Exception {
            ConvertData(video);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    private static void ConvertData(Video video) throws Exception {
        val videoInfoGetter = new VideoInfoGetter();
        val hiveController = new HiveHelper();
        val result = new ArrayList<>();
        for(int i = 0; i < video.getFramesInfo().size(); i++){
            VideoInfo videoInfo = new VideoInfo();
            videoInfo.setVideoId(video.getVideoInfo().getVideoId());
            //val startTime = "2018-01-01T12:00:00Z";
            val startTime = videoInfoGetter.getVideoStartTime(video.getVideoInfo().getVideoId());
            videoInfo.setStartTime(startTime);
            val relativeTime = video.getFramesInfo().get(i).getFrameInfo().get(0).getRelativeTime();
            val frameInfo = new FrameInfo();
            frameInfo.setRelativeTime(relativeTime);
            val absoluteTime = absoluteTime(startTime,relativeTime);
            frameInfo.setAbsoluteTime(absoluteTime);
            CameraPosition cameraPosition = new CameraPosition();
            Double Lat = Double.parseDouble(videoInfoGetter.getCameraLat(video.getVideoInfo().getVideoId()));
            Double Lon = Double.parseDouble(videoInfoGetter.getCameraLon(video.getVideoInfo().getVideoId()));
            cameraPosition.setLat(Lat);
            cameraPosition.setLon(Lon);
            CameraInfo cameraInfo = new CameraInfo();
            cameraInfo.setCameraId(videoInfoGetter.getCameraId(video.getVideoInfo().getVideoId()));
            cameraInfo.setCameraName(videoInfoGetter.getCameraName(video.getVideoInfo().getVideoId()));
            cameraInfo.setPosition(cameraPosition);
            for(int j = 0; j < video.getFramesInfo().get(i).getCars().size();j++){

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
            //val startTime = "2018-01-01T12:00:00Z";
            val startTime = videoInfoGetter.getVideoStartTime(video.getVideoInfo().getVideoId());
            videoInfo.setStartTime(startTime);
            val relativeTime = video.getFramesInfo().get(i).getFrameInfo().get(0).getRelativeTime();
            val frameInfo = new FrameInfo();
            frameInfo.setRelativeTime(relativeTime);
            val absoluteTime = absoluteTime(startTime,relativeTime);
            frameInfo.setAbsoluteTime(absoluteTime);
            CameraPosition cameraPosition = new CameraPosition();
            Double Lat = Double.parseDouble(videoInfoGetter.getCameraLat(video.getVideoInfo().getVideoId()));
            Double Lon = Double.parseDouble(videoInfoGetter.getCameraLon(video.getVideoInfo().getVideoId()));
            cameraPosition.setLat(Lat);
            cameraPosition.setLon(Lon);
            CameraInfo cameraInfo = new CameraInfo();
            cameraInfo.setCameraId(videoInfoGetter.getCameraId(video.getVideoInfo().getVideoId()));
            cameraInfo.setCameraName(videoInfoGetter.getCameraName(video.getVideoInfo().getVideoId()));
            cameraInfo.setPosition(cameraPosition);
            for(int j = 0; j < video.getFramesInfo().get(i).getPeople().size();j++){
                Person person = video.getFramesInfo().get(i).getPeople().get(j);
                PersonTask personTask = new PersonTask(person, videoInfo,cameraInfo,frameInfo);
                result.add(personTask);
//                String sql = "insert into object_data values（"+ personTask.video_information+","+personTask.camera_info+","
//                        +personTask.person+",null,"+personTask.frame_info+")";
//                hiveController.executedNoquery(sql);
            }
        }

        try (val uploader = new EsUploader(ELASTIC_SEARCH_URL, INDEX_NAME, TYPE, BATCH_SIZE)) {
//            if (uploader.indexExists()) {
//                uploader.deleteIndex();
//            }
            if (!uploader.indexExists()) {
                uploader.createIndex("camera_info.position", "type=geo_point");
            }
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
