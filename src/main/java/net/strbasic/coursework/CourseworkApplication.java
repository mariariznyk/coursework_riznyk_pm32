package net.strbasic.coursework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  
public class CourseworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseworkApplication.class, args);
    }
}
