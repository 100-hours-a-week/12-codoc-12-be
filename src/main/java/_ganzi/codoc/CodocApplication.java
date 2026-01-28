package _ganzi.codoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CodocApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodocApplication.class, args);
    }
}
