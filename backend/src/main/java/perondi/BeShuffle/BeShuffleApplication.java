package perondi.BeShuffle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Chaves e senhas vêm de infra/.env, lido pelo próprio Spring (spring.config.import em application.properties).
 */
@SpringBootApplication
@EnableScheduling
public class BeShuffleApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeShuffleApplication.class, args);
	}

}
