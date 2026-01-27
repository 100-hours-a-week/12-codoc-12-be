package _ganzi.codoc.submission.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.submission.dto.ProblemSubmissionResponse;
import _ganzi.codoc.submission.dto.QuizGradingRequest;
import _ganzi.codoc.submission.dto.QuizGradingResponse;
import _ganzi.codoc.submission.dto.SummaryCardGradingRequest;
import _ganzi.codoc.submission.dto.SummaryCardGradingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Submission", description = "Submission grading endpoints")
public interface SubmissionApi {

    @Operation(summary = "Grade summary cards")
    ResponseEntity<ApiResponse<SummaryCardGradingResponse>> gradeSummaryCards(
            AuthUser authUser, SummaryCardGradingRequest request);

    @Operation(summary = "Grade quiz submission")
    ResponseEntity<ApiResponse<QuizGradingResponse>> gradeQuiz(
            AuthUser authUser, Long quizId, QuizGradingRequest request);

    @Operation(summary = "Submit problem solution")
    ResponseEntity<ApiResponse<ProblemSubmissionResponse>> submissionProblem(
            AuthUser authUser, Long problemId);
}
