package _ganzi.codoc.user.api.dto;

import _ganzi.codoc.user.domain.DailyGoal;
import _ganzi.codoc.user.domain.InitLevel;
import jakarta.validation.constraints.NotNull;

public record UserInitSurveyRequest(
        @NotNull(message = "초기 레벨 값이 유효하지 않습니다.") InitLevel initLevel,
        @NotNull(message = "일일 목표 값이 유효하지 않습니다.") DailyGoal dailyGoal) {}
