package _ganzi.codoc.auth.service;

import _ganzi.codoc.auth.service.dto.KakaoTokenResponse;
import _ganzi.codoc.auth.service.dto.KakaoUserResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class KakaoOAuthClient {

    private static final String TOKEN_URI = "https://kauth.kakao.com/oauth/token";
    private static final String USER_URI = "https://kapi.kakao.com/v2/user/me";

    private final WebClient webClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.client-secret:}")
    private String clientSecret;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoTokenResponse exchangeToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", clientId);
        form.add("redirect_uri", redirectUri);
        form.add("code", code);
        if (!clientSecret.isBlank()) {
            form.add("client_secret", clientSecret);
        }

        return webClient
                .post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

    public KakaoUserResponse fetchUser(String accessToken) {
        Map<String, Object> response =
                webClient
                        .post()
                        .uri(USER_URI)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
        String id = String.valueOf(response.get("id"));
        Map<String, Object> properties = (Map<String, Object>) response.get("properties");
        String nickname = properties != null ? String.valueOf(properties.get("nickname")) : null;
        return new KakaoUserResponse(id, nickname);
    }
}
