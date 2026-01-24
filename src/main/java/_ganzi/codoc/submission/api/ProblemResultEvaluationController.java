package _ganzi.codoc.submission.api;

import _ganzi.codoc.global.annotation.CurrentUserId;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.submission.dto.ProblemResultEvaluationResponse;
import _ganzi.codoc.submission.service.ProblemResultEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/problems")
@RestController
public class ProblemResultEvaluationController {

    private final ProblemResultEvaluationService problemResultEvaluationService;

    @PostMapping("/{problemId}/evaluation")
    public ResponseEntity<ApiResponse<ProblemResultEvaluationResponse>> evaluateProblemResult(
            @CurrentUserId Long userId, @PathVariable Long problemId) {

        ProblemResultEvaluationResponse response =
                problemResultEvaluationService.evaluateProblemResult(userId, problemId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
