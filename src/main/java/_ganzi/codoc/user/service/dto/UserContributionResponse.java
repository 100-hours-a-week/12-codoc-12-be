package _ganzi.codoc.user.service.dto;

import java.time.LocalDate;
import java.util.List;

public record UserContributionResponse(List<DailySolveCount> dailySolveCount) {

    public record DailySolveCount(LocalDate date, int solveCount) {}
}
