package pe.contrataia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ContrataIAApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContrataIAApplication.class, args);
    }
}
