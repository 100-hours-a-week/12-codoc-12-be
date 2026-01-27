package _ganzi.codoc.submission.api;

import _ganzi.codoc.global.annotation.CurrentUserId;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.submission.dto.*;
import _ganzi.codoc.submission.service.ProblemSubmissionService;
import _ganzi.codoc.submission.service.QuizSubmissionService;
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
@RequestMapping("/api")
@RestController
public class SubmissionController {

    private final SummaryCardSubmissionService summaryCardSubmissionService;
    private final QuizSubmissionService quizSubmissionService;
    private final ProblemSubmissionService problemSubmissionService;

    @PostMapping("/summary-cards/submissions")
    public ResponseEntity<ApiResponse<SummaryCardGradingResponse>> gradeSummaryCards(
            @CurrentUserId Long userId, @Valid @RequestBody SummaryCardGradingRequest request) {

        SummaryCardGradingResponse response =
                summaryCardSubmissionService.gradeSummaryCards(userId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/quizzes/{quizId}/submissions")
    public ResponseEntity<ApiResponse<QuizGradingResponse>> gradeQuiz(
            @CurrentUserId Long userId,
            @PathVariable Long quizId,
            @Valid @RequestBody QuizGradingRequest request) {

        QuizGradingResponse response = quizSubmissionService.gradeQuiz(userId, quizId, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/problems/{problemId}/submissions")
    public ResponseEntity<ApiResponse<ProblemSubmissionResponse>> submissionProblem(
            @CurrentUserId Long userId, @PathVariable Long problemId) {

        ProblemSubmissionResponse response =
                problemSubmissionService.submissionProblem(userId, problemId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
