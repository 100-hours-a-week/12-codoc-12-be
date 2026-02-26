package _ganzi.codoc.submission.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import _ganzi.codoc.problem.exception.ProblemErrorCode;
import _ganzi.codoc.submission.dto.ProblemSubmissionResponse;
import _ganzi.codoc.submission.dto.QuizGradingRequest;
import _ganzi.codoc.submission.dto.QuizGradingResponse;
import _ganzi.codoc.submission.dto.SummaryCardGradingRequest;
import _ganzi.codoc.submission.dto.SummaryCardGradingResponse;
import _ganzi.codoc.submission.exception.SubmissionErrorCode;
import _ganzi.codoc.user.exception.UserErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Submission", description = "Submission grading endpoints")
public interface SubmissionApi {

    @Operation(summary = "Grade summary cards")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_INPUT, INVALID_ANSWER_FORMAT, SESSION_REQUIRED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "PROBLEM_NOT_FOUND, SUMMARY_CARD_NOT_FOUND, USER_NOT_FOUND"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.INVALID_INPUT,
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            submission = {
                SubmissionErrorCode.INVALID_ANSWER_FORMAT,
                SubmissionErrorCode.SESSION_REQUIRED
            },
            problem = {ProblemErrorCode.PROBLEM_NOT_FOUND, ProblemErrorCode.SUMMARY_CARD_NOT_FOUND},
            user = {UserErrorCode.USER_NOT_FOUND})
    ResponseEntity<ApiResponse<SummaryCardGradingResponse>> gradeSummaryCards(
            AuthUser authUser, SummaryCardGradingRequest request);

    @Operation(summary = "Grade quiz submission")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description =
                        "INVALID_INPUT, INVALID_ANSWER_FORMAT, QUIZ_GRADING_NOT_ALLOWED,"
                                + " INVALID_QUIZ_ATTEMPT, PREV_QUIZ_NOT_SUBMITTED, QUIZ_ALREADY_SUBMITTED,"
                                + " SESSION_REQUIRED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "QUIZ_NOT_FOUND, USER_NOT_FOUND"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.INVALID_INPUT,
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            submission = {
                SubmissionErrorCode.INVALID_ANSWER_FORMAT,
                SubmissionErrorCode.QUIZ_GRADING_NOT_ALLOWED,
                SubmissionErrorCode.INVALID_QUIZ_ATTEMPT,
                SubmissionErrorCode.PREV_QUIZ_NOT_SUBMITTED,
                SubmissionErrorCode.QUIZ_ALREADY_SUBMITTED,
                SubmissionErrorCode.SESSION_REQUIRED
            },
            problem = {ProblemErrorCode.QUIZ_NOT_FOUND},
            user = {UserErrorCode.USER_NOT_FOUND})
    ResponseEntity<ApiResponse<QuizGradingResponse>> gradeQuiz(
            AuthUser authUser, Long quizId, QuizGradingRequest request);

    @Operation(summary = "Submit problem solution")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_INPUT, INVALID_PROBLEM_SUBMISSION, SESSION_REQUIRED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "PROBLEM_NOT_FOUND, USER_NOT_FOUND"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.INVALID_INPUT,
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            submission = {
                SubmissionErrorCode.INVALID_PROBLEM_SUBMISSION,
                SubmissionErrorCode.SESSION_REQUIRED
            },
            problem = {ProblemErrorCode.PROBLEM_NOT_FOUND},
            user = {UserErrorCode.USER_NOT_FOUND})
    ResponseEntity<ApiResponse<ProblemSubmissionResponse>> submissionProblem(
            AuthUser authUser, Long problemId);
}
