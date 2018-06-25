package boston.convertdata.model.structured;

import lombok.Data;

import java.util.List;

@Data
public class Segment {
    private String segmentId;
    private Car car;
    private Person person;
    private List<Frame> framesInfo;
    private String objectImg;

}
