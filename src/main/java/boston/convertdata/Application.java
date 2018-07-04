package boston.convertdata;

import boston.convertdata.service.Monitoring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("boston.convertdata")
public class Application implements CommandLineRunner {

    @Autowired
    private Monitoring monitoring;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        monitoring.start();
    }
}
