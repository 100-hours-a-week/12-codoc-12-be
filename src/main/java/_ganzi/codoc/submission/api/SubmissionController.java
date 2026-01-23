package _ganzi.codoc.submission.api;

import _ganzi.codoc.global.annotation.CurrentUserId;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.submission.dto.SummaryCardGradingRequest;
import _ganzi.codoc.submission.dto.SummaryCardGradingResponse;
import _ganzi.codoc.submission.service.SummaryCardSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/submission")
@RestController
public class SubmissionController {

    private final SummaryCardSubmissionService summaryCardSubmissionService;

    @PostMapping("/problems/{problemId}/summary-cards")
    public ResponseEntity<ApiResponse<SummaryCardGradingResponse>> gradeSummaryCards(
            @CurrentUserId Long userId,
            @PathVariable Long problemId,
            @Valid @RequestBody SummaryCardGradingRequest request) {

        SummaryCardGradingResponse response =
                summaryCardSubmissionService.gradeSummaryCards(userId, problemId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
