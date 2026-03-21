package _ganzi.codoc.custom.service;

import _ganzi.codoc.ai.dto.CustomProblemGenerationRequest;
import _ganzi.codoc.custom.domain.CustomProblem;
import _ganzi.codoc.custom.enums.CustomProblemStatus;
import _ganzi.codoc.custom.infra.CustomProblemStorageService;
import _ganzi.codoc.custom.repository.CustomProblemRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomProblemGenerationOrchestrator {

    private final CustomProblemRepository customProblemRepository;
    private final CustomProblemStorageService customProblemStorageService;
    private final CustomProblemGenerationRequestProducer requestProducer;
    private final CustomProblemGenerationResultService resultService;

    public void process(Long customProblemId) {
        CustomProblem customProblem =
                customProblemRepository.findActiveById(customProblemId).orElse(null);
        if (customProblem == null || customProblem.getStatus() != CustomProblemStatus.PROCESSING) {
            return;
        }

        List<CustomProblemGenerationRequest> images;
        try {
            images = buildImages(customProblem.getSourceFileKeys());
        } catch (Exception exception) {
            log.warn(
                    "Custom problem generation image preparation failed. customProblemId={}",
                    customProblemId,
                    exception);
            resultService.fail(customProblemId);
            return;
        }

        try {
            requestProducer.produce(customProblemId, images);
        } catch (Exception exception) {
            log.warn(
                    "Custom problem generation request produce failed. customProblemId={}",
                    customProblemId,
                    exception);
            resultService.fail(customProblemId);
        }
    }

    private List<CustomProblemGenerationRequest> buildImages(List<String> sourceFileKeys) {
        List<String> readableUrls = customProblemStorageService.createReadableUrls(sourceFileKeys);

        List<CustomProblemGenerationRequest> images = new ArrayList<>(readableUrls.size());
        for (int index = 0; index < readableUrls.size(); index++) {
            images.add(new CustomProblemGenerationRequest(index + 1, readableUrls.get(index)));
        }
        return images;
    }
}
