package boston.convertdata.model.elasticsearch;

import lombok.Data;

import java.time.Instant;

@Data
public class VideoInfo {
    private String videoId;
    private String videoUrl;
    private Instant startTime;
}
