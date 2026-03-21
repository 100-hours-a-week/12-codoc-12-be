package _ganzi.codoc.custom.dto;

import _ganzi.codoc.custom.domain.CustomProblem;
import _ganzi.codoc.custom.enums.CustomProblemStatus;
import java.time.Instant;

public record CustomProblemListItem(
        Long customProblemId, String title, CustomProblemStatus status, Instant createdAt) {

    public static CustomProblemListItem from(CustomProblem customProblem) {
        return new CustomProblemListItem(
                customProblem.getId(),
                customProblem.getTitle(),
                customProblem.getStatus(),
                customProblem.getCreatedAt());
    }
}
