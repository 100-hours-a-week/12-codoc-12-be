package _ganzi.codoc.analysis.infra;

import _ganzi.codoc.analysis.config.AnalysisServerProperties;
import _ganzi.codoc.analysis.dto.AnalysisReportRequest;
import _ganzi.codoc.analysis.dto.AnalysisReportResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class AnalysisReportClient {

    private static final String API_PATH_REPORT = "/api/v2/reports";

    private final WebClient.Builder analysisServerWebClientBuilder;
    private final AnalysisServerProperties analysisServerProperties;
    private final JsonMapper jsonMapper;

    public AnalysisReportClient(
            @Qualifier("analysisServerWebClientBuilder") WebClient.Builder analysisServerWebClientBuilder,
            AnalysisServerProperties analysisServerProperties,
            JsonMapper jsonMapper) {
        this.analysisServerWebClientBuilder = analysisServerWebClientBuilder;
        this.analysisServerProperties = analysisServerProperties;
        this.jsonMapper = jsonMapper;
    }

    public AnalysisReportResponse requestReport(AnalysisReportRequest request) {
        return analysisServerWebClientBuilder
                .build()
                .post()
                .uri(API_PATH_REPORT)
                .bodyValue(request)
                .exchangeToMono(
                        response -> response.bodyToMono(String.class).map(body -> parseResponse(body)))
                .timeout(analysisServerProperties.baseTimeout())
                .block();
    }

    private AnalysisReportResponse parseResponse(String body) {
        if (body == null || body.isBlank()) {
            log.warn("analysis report response body empty");
            return null;
        }

        try {
            return jsonMapper.readValue(body, AnalysisReportResponse.class);
        } catch (JacksonException ex) {
            log.warn("analysis report response parse failed. body={}", body);
            return null;
        }
    }
}
