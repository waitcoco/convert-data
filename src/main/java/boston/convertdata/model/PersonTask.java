package boston.convertdata.model;

import lombok.Data;

@Data
public class PersonTask {
    private Person person;
    private VideoInformation videoInformation;
    private CameraInfo cameraInfo;
    private FrameInfo frameInfo;

    public PersonTask(Person person, VideoInformation videoInformation, CameraInfo camera_info, FrameInfo frame_info){
        this.person = person;
        this.videoInformation = videoInformation;
        this.cameraInfo = camera_info;
        this.frameInfo = frame_info;
    }
}
