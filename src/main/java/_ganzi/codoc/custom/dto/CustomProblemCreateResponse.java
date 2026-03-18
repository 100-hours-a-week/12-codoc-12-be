package _ganzi.codoc.custom.dto;

import _ganzi.codoc.custom.domain.CustomProblem;
import _ganzi.codoc.custom.enums.CustomProblemStatus;
import java.time.Instant;

public record CustomProblemCreateResponse(
        Long customProblemId, String title, CustomProblemStatus status, Instant createdAt) {

    public static CustomProblemCreateResponse from(CustomProblem customProblem) {
        return new CustomProblemCreateResponse(
                customProblem.getId(),
                customProblem.getTitle(),
                customProblem.getStatus(),
                customProblem.getCreatedAt());
    }
}
