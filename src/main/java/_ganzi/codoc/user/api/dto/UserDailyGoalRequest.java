package _ganzi.codoc.user.api.dto;

import _ganzi.codoc.user.enums.DailyGoal;
import jakarta.validation.constraints.NotNull;

public record UserDailyGoalRequest(@NotNull(message = "일일 목표 값이 유효하지 않습니다.") DailyGoal dailyGoal) {}
