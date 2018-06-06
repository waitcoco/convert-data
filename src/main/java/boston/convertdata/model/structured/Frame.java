package boston.convertdata.model.structured;

import lombok.Data;

import java.util.List;

@Data
public class Frame {
 private List<Car> cars;
 private List<Person> people;
 private List<FrameInfo> frameInfo;
}
