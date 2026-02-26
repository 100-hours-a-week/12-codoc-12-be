package _ganzi.codoc.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public record AnalysisReportResponse(String code, String message, Data data) {

    public record Data(
            @JsonProperty("user_id") long userId,
            @JsonProperty("analysis_period") AnalysisPeriod analysisPeriod,
            JsonNode report) {}
}
