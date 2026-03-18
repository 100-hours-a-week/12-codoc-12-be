package _ganzi.codoc.custom.service;

import _ganzi.codoc.custom.domain.CustomProblem;
import _ganzi.codoc.custom.dto.*;
import _ganzi.codoc.custom.event.CustomProblemGenerationRequestedEvent;
import _ganzi.codoc.custom.infra.CustomProblemStorageService;
import _ganzi.codoc.custom.repository.CustomProblemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CustomProblemService {

    private final CustomProblemImageRequestValidator imageRequestValidator;
    private final CustomProblemStorageService customProblemStorageService;
    private final CustomProblemRepository customProblemRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public CustomProblemCreateResponse createCustomProblem(
            Long userId, CustomProblemCreateRequest request) {
        imageRequestValidator.validateCreateImages(request.images());

        List<String> orderedFileKeys =
                request.images().stream()
                        .sorted(java.util.Comparator.comparing(CustomProblemCreateRequest.ImageItem::order))
                        .map(CustomProblemCreateRequest.ImageItem::fileKey)
                        .toList();

        customProblemStorageService.validateSourceImagesOwnedAndExisting(userId, orderedFileKeys);

        CustomProblem customProblem =
                customProblemRepository.save(CustomProblem.createProcessing(userId, orderedFileKeys));

        applicationEventPublisher.publishEvent(
                new CustomProblemGenerationRequestedEvent(customProblem.getId()));

        return CustomProblemCreateResponse.from(customProblem);
    }
}
