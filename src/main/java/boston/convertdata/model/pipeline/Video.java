package boston.convertdata.model.pipeline;

import lombok.Data;

import java.time.Instant;

@Data
public class Video {
    private String id;
    private String cameraId;
    private String playbackUrl;
    private Instant startTime;
}
