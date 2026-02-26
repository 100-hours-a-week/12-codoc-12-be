package _ganzi.codoc.analysis.service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;

public record AnalysisReportDetailResponse(
        LocalDate periodStart, LocalDate periodEnd, JsonNode report) {}
