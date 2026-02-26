package _ganzi.codoc.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record AnalysisPeriod(
        @JsonProperty("start_date") LocalDate startDate, @JsonProperty("end_date") LocalDate endDate) {}
