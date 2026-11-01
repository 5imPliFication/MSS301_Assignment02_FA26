package fu.he190091.watch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MSWatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MSWatchApplication.class, args);
    }
}
