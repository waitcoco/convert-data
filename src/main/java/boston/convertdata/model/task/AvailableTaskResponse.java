package boston.convertdata.model.task;

import lombok.Data;

@Data
public class AvailableTaskResponse {
    private boolean found;
    private Task task;
    private Video video;
}
