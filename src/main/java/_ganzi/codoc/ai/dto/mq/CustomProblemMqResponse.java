package _ganzi.codoc.ai.dto.mq;

import _ganzi.codoc.ai.dto.AiServerApiResponse;
import _ganzi.codoc.ai.dto.CustomProblemGenerationResponse;

public record CustomProblemMqResponse(
        Long customProblemId, AiServerApiResponse<CustomProblemGenerationResponse> response) {}
