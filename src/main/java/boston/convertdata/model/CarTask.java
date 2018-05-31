package boston.convertdata.model;

import lombok.Data;

@Data
public class CarTask {
    private VideoInfo videoInfo;
    private CameraInfo cameraInfo;
    private FrameInfo frameInfo;
    private Car car;

    public CarTask(Car car, VideoInfo videoInfo, CameraInfo cameraInfo, FrameInfo frameInfo){
        this.car = car;
        this.videoInfo = videoInfo;
        this.cameraInfo = cameraInfo;
        this.frameInfo = frameInfo;
    }
}
