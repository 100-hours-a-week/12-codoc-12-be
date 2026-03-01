package _ganzi.codoc.analysis.service.dto;

import _ganzi.codoc.analysis.dto.AnalysisPeriod;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;

public record AnalysisReportDetailResponse(
        @JsonProperty("user_id") long userId,
        @JsonProperty("analysis_period") AnalysisPeriod analysisPeriod,
        @JsonRawValue @JsonProperty("report") String report) {}
