package _ganzi.codoc.problem.api;

import _ganzi.codoc.global.annotation.CurrentUserId;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.problem.dto.ProblemListCondition;
import _ganzi.codoc.problem.dto.ProblemListResponse;
import _ganzi.codoc.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/problems")
@RestController
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProblemListResponse>> getProblemList(
            @CurrentUserId Long userId, @Valid ProblemListCondition condition) {

        ProblemListResponse response = problemService.getProblemList(userId, condition);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
