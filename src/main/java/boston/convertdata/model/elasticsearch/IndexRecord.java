package boston.convertdata.model.elasticsearch;

import boston.convertdata.model.structured.Car;
import boston.convertdata.model.structured.FrameInfo;
import boston.convertdata.model.structured.Person;
import lombok.Data;

@Data
public class IndexRecord {
    private VideoInfo videoInfo;
    private CameraInfo cameraInfo;
    private FrameInfo frameInfo;
    private Car car;
    private Person person;

    public IndexRecord(Person person, VideoInfo videoInfo, CameraInfo cameraInfo, FrameInfo frameInfo){
        this.person = person;
        this.videoInfo = videoInfo;
        this.cameraInfo = cameraInfo;
        this.frameInfo = frameInfo;
    }

    public IndexRecord(Car car, VideoInfo videoInfo, CameraInfo cameraInfo, FrameInfo frameInfo){
        this.car = car;
        this.videoInfo = videoInfo;
        this.cameraInfo = cameraInfo;
        this.frameInfo = frameInfo;
    }
}
