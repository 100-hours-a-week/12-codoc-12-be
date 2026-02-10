package _ganzi.codoc.problem.service.recommend;

import _ganzi.codoc.ai.config.AiServerProperties;
import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import _ganzi.codoc.problem.service.recommend.dto.RecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class RecommendClient {

    private static final String API_PATH_RECOMMEND = "/api/v2/recommend";

    private final WebClient.Builder aiServerWebClientBuilder;
    private final AiServerProperties aiServerProperties;

    public RecommendResponse requestRecommendations(RecommendRequest request) {
        return aiServerWebClientBuilder
                .build()
                .post()
                .uri(API_PATH_RECOMMEND)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RecommendResponse.class)
                .timeout(aiServerProperties.baseTimeout())
                .block();
    }
}
