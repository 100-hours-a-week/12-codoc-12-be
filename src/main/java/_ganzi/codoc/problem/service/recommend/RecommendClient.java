package _ganzi.codoc.problem.service.recommend;

import _ganzi.codoc.analysis.config.AnalysisServerProperties;
import _ganzi.codoc.problem.service.recommend.dto.RecommendRequest;
import _ganzi.codoc.problem.service.recommend.dto.RecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class RecommendClient {

    private static final String API_PATH_RECOMMEND = "/api/v2/recommend";

    @Qualifier("analysisServerWebClientBuilder")
    private final WebClient.Builder analysisServerWebClientBuilder;

    private final AnalysisServerProperties analysisServerProperties;

    public RecommendResponse requestRecommendations(RecommendRequest request) {
        return analysisServerWebClientBuilder
                .build()
                .post()
                .uri(API_PATH_RECOMMEND)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RecommendResponse.class)
                .timeout(analysisServerProperties.recommendTimeout())
                .block();
    }
}
