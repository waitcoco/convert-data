package boston.convertdata.model.structured;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Video {
    private String videoId;
    private List<Segment> segmentsInfo;
}
