package perondi.BeShuffle.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Relógio da aplicação no fuso do público (padrão America/Sao_Paulo), para que o
 * "dia" do álbum do dia não dependa do fuso do servidor (containers rodam em UTC).
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock(@Value("${beshuffle.timezone:America/Sao_Paulo}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
