package boston.convertdata.model.elasticsearch;

import boston.convertdata.model.structured.Car;
import boston.convertdata.model.structured.Frame;
import boston.convertdata.model.structured.Person;
import lombok.Data;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

@Data
public class Segment {
    private String segmentId;
    private Instant startTime;
    private Instant endTime;
    private LocalTime relativeStartTime;
    private LocalTime relativeEndTime;

    private VideoInfo videoInfo;
    private CameraInfo cameraInfo;
    private List<Frame> framesInfo;
    private Car car;
    private Person person;

    private String objectImgUrl;
}
