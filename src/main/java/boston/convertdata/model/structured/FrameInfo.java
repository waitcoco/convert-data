package boston.convertdata.model.structured;

import lombok.Data;

import java.time.Instant;
import java.time.LocalTime;

@Data
public class FrameInfo {
    private LocalTime relativeTime;
    private Instant absoluteTime;
}
