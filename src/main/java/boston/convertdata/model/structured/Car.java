package boston.convertdata.model.structured;

import lombok.Data;

@Data
public class Car {
    private String carId;
    private String type;
    private String make;
    private String model;
    private String carColor;
    private String licensePlateNo;
    private String licensePlateColor;
}
