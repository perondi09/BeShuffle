package perondi.BeShuffle.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class ApplicationConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
    }

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        String defaultDb = env.getProperty("DB_NAME", "beshuffle_db");
        String defaultUrl = String.format("jdbc:postgresql://localhost:5435/%s", defaultDb);

        String url = env.getProperty("SPRING_DATASOURCE_URL", defaultUrl);
        String user = env.getProperty("SPRING_DATASOURCE_USERNAME", env.getProperty("DB_USER", "postgres"));
        String password = env.getProperty("SPRING_DATASOURCE_PASSWORD", env.getProperty("DB_PASSWORD", ""));

        // If the configured host is not resolvable from this environment (e.g. 'db' only resolves inside Docker),
        // fall back to localhost development port 5435. This allows running the app in IDE without Docker.
        try {
            Pattern p = Pattern.compile("jdbc:postgresql://([^:/\\s]+)(?::(\\d+))?/(.*)");
            Matcher m = p.matcher(url);
            if (m.find()) {
                String host = m.group(1);
                try {
                    InetAddress.getByName(host);
                } catch (Exception ex) {
                    // host not resolvable — fallback
                    url = defaultUrl;
                }
            }
        } catch (Exception ignored) {
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setPoolName("HikariPool-Local");
        return new HikariDataSource(config);
    }
}