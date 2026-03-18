package _ganzi.codoc.ai.dto.mq;

import _ganzi.codoc.ai.dto.CustomProblemGenerationRequest;
import java.util.List;

public record CustomProblemMqRequest(
        Long customProblemId, List<CustomProblemGenerationRequest> images) {}
