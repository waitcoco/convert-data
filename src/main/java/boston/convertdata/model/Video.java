package boston.convertdata.model;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Video {
    private VideoInformation videoInfo;
    private ArrayList<Frame> framesInfo;
}
