package boston.convertdata.model.structured;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
public class Frame {
    private LocalTime relativeTime;
    private String objectLefttop;
    private String objectLeftbottom;
    private String objectRighttop;
    private String objectRightbottom;
}
