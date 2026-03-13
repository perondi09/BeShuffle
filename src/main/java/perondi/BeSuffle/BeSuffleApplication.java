package perondi.BeSuffle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BeSuffleApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeSuffleApplication.class, args);
	}

}
