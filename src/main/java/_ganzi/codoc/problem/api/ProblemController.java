package _ganzi.codoc.problem.api;

import _ganzi.codoc.global.annotation.CurrentUserId;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.problem.dto.ProblemListCondition;
import _ganzi.codoc.problem.dto.ProblemListItem;
import _ganzi.codoc.problem.dto.ProblemResponse;
import _ganzi.codoc.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/problems")
@RestController
public class ProblemController {

    private final ProblemService problemService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPagingResponse<ProblemListItem, Long>>> getProblemList(
            @CurrentUserId Long userId, @Valid ProblemListCondition condition) {

        CursorPagingResponse<ProblemListItem, Long> response =
                problemService.getProblemList(userId, condition);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPagingResponse<ProblemListItem, Long>>> searchProblems(
            @CurrentUserId Long userId, @Valid ProblemListCondition condition) {

        CursorPagingResponse<ProblemListItem, Long> response =
                problemService.searchProblems(userId, condition);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemDetail(
            @CurrentUserId Long userId, @PathVariable Long problemId) {

        ProblemResponse response = problemService.getProblemDetail(userId, problemId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
