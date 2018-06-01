package boston.convertdata.model;

import lombok.Data;

@Data
public class PersonTask {
    private VideoInfo videoInfo;
    private CameraInfo cameraInfo;
    private FrameInfo frameInfo;
    private Person person;

    public PersonTask(Person person, VideoInfo videoInfo, CameraInfo camera_info, FrameInfo frame_info){
        this.person = person;
        this.videoInfo = videoInfo;
        this.cameraInfo = camera_info;
        this.frameInfo = frame_info;
    }
}
