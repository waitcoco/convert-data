package boston.convertdata.model.elasticsearch;

import lombok.Data;

@Data
public class CameraInfo {
    private String cameraId;
    private String cameraName;
    private CameraPosition position;

}
