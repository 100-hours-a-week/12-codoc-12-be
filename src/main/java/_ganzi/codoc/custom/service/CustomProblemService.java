package _ganzi.codoc.custom.service;

import _ganzi.codoc.custom.domain.CustomProblem;
import _ganzi.codoc.custom.dto.*;
import _ganzi.codoc.custom.event.CustomProblemGenerationRequestedEvent;
import _ganzi.codoc.custom.exception.*;
import _ganzi.codoc.custom.infra.CustomProblemStorageService;
import _ganzi.codoc.custom.repository.CustomProblemRepository;
import _ganzi.codoc.custom.repository.CustomQuizRepository;
import _ganzi.codoc.custom.repository.CustomSummaryCardRepository;
import _ganzi.codoc.global.cursor.CursorPageFetcher;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import java.time.Instant;
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
    private final CustomQuizRepository customQuizRepository;
    private final CustomSummaryCardRepository customSummaryCardRepository;
    private final CursorPageFetcher cursorPageFetcher;
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

    public CursorPagingResponse<CustomProblemListItem, String> getCustomProblems(
            Long userId, String cursor, Integer limit) {
        return cursorPageFetcher.fetch(
                cursor,
                limit,
                CustomProblemCursorPayload.class,
                CustomProblemCursorPayload::firstPage,
                (cursorPayload, pageable) ->
                        customProblemRepository.findByUserIdWithCursor(
                                userId, cursorPayload.createdAt(), cursorPayload.customProblemId(), pageable),
                customProblems -> customProblems.stream().map(CustomProblemListItem::from).toList(),
                CustomProblemCursorPayload::from);
    }

    public CustomProblemDetailResponse getCustomProblemDetail(Long userId, Long customProblemId) {
        CustomProblem customProblem =
                customProblemRepository
                        .findActiveById(customProblemId)
                        .orElseThrow(CustomProblemNotFoundException::new);

        validateOwner(userId, customProblem);
        validateCompleted(customProblem);

        List<CustomSummaryCardResponse> summaryCards =
                customSummaryCardRepository.findAllByCustomProblemId(customProblemId).stream()
                        .map(CustomSummaryCardResponse::from)
                        .toList();
        List<CustomQuizResponse> quizzes =
                customQuizRepository.findAllByCustomProblemId(customProblemId).stream()
                        .map(CustomQuizResponse::from)
                        .toList();

        return CustomProblemDetailResponse.of(customProblem, summaryCards, quizzes);
    }

    @Transactional
    public void deleteCustomProblem(Long userId, Long customProblemId) {
        CustomProblem customProblem =
                customProblemRepository
                        .findActiveById(customProblemId)
                        .orElseThrow(CustomProblemNotFoundException::new);

        validateOwner(userId, customProblem);

        customProblem.markDeleted(Instant.now());
    }

    private void validateOwner(Long userId, CustomProblem customProblem) {
        if (!customProblem.getUserId().equals(userId)) {
            throw new CustomProblemNoPermissionException();
        }
    }

    private void validateCompleted(CustomProblem customProblem) {
        if (!customProblem.getStatus().isCompleted()) {
            throw new CustomProblemNotCompletedException();
        }
    }
}
