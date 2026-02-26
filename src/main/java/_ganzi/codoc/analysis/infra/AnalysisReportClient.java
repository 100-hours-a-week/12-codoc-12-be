package _ganzi.codoc.analysis.infra;

import _ganzi.codoc.analysis.config.AnalysisServerProperties;
import _ganzi.codoc.analysis.dto.AnalysisReportRequest;
import _ganzi.codoc.analysis.dto.AnalysisReportResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AnalysisReportClient {

    private static final String API_PATH_REPORT = "/api/v2/reports";

    private final WebClient.Builder analysisServerWebClientBuilder;
    private final AnalysisServerProperties analysisServerProperties;

    public AnalysisReportClient(
            @Qualifier("analysisServerWebClientBuilder") WebClient.Builder analysisServerWebClientBuilder,
            AnalysisServerProperties analysisServerProperties) {
        this.analysisServerWebClientBuilder = analysisServerWebClientBuilder;
        this.analysisServerProperties = analysisServerProperties;
    }

    public AnalysisReportResponse requestReport(AnalysisReportRequest request) {
        return analysisServerWebClientBuilder
                .build()
                .post()
                .uri(API_PATH_REPORT)
                .bodyValue(request)
                .exchangeToMono(response -> response.bodyToMono(AnalysisReportResponse.class))
                .timeout(analysisServerProperties.baseTimeout())
                .block();
    }
}
