package boston.convertdata.model;

import lombok.Data;

@Data
public class CarTask {
    private VideoInformation videoInformation;
    private CameraInfo cameraInfo;
    private FrameInfo frameInfo;
    private Car car;

    public CarTask(Car car, VideoInformation videoInformation, CameraInfo cameraInfo, FrameInfo frameInfo){
        this.car = car;
        this.videoInformation = videoInformation;
        this.cameraInfo = cameraInfo;
        this.frameInfo = frameInfo;
    }
}
