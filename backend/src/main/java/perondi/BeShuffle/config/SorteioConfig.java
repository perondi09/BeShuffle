package perondi.BeShuffle.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.random.RandomGenerator;

@Configuration
@EnableConfigurationProperties(SorteioProperties.class)
public class SorteioConfig {

    /** Gerador do sorteio; um bean para os testes poderem trocar por um com semente fixa. */
    @Bean
    public RandomGenerator sorteioRandom() {
        return RandomGenerator.getDefault();
    }
}
