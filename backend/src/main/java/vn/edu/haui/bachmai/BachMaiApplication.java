package vn.edu.haui.bachmai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BachMaiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BachMaiApplication.class, args);
    }
}
