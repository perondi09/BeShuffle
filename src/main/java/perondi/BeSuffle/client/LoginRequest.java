package perondi.BeSuffle.client;

import feign.form.FormProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class LoginRequest {

    @FormProperty("grant_type")
    private String grantType;

    @FormProperty("client_id")
    @Value("${api.client.id}")
    private String clientId;

    @FormProperty("client_secret")
    @Value("${api.client.secret}")
    private String clientSecret;

}
