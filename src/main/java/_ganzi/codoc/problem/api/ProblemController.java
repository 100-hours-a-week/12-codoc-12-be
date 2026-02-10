package _ganzi.codoc.problem.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.problem.dto.ProblemListCondition;
import _ganzi.codoc.problem.dto.ProblemListItem;
import _ganzi.codoc.problem.dto.ProblemResponse;
import _ganzi.codoc.problem.dto.RecommendedProblemResponse;
import _ganzi.codoc.problem.service.ProblemBookmarkService;
import _ganzi.codoc.problem.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/problems")
@RestController
public class ProblemController implements ProblemApi {

    private final ProblemBookmarkService problemBookmarkService;
    private final ProblemService problemService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPagingResponse<ProblemListItem, Long>>> getProblemList(
            @AuthenticationPrincipal AuthUser authUser, @Valid ProblemListCondition condition) {

        CursorPagingResponse<ProblemListItem, Long> response =
                problemService.getProblemList(authUser.userId(), condition);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPagingResponse<ProblemListItem, Long>>> searchProblems(
            @AuthenticationPrincipal AuthUser authUser, @Valid ProblemListCondition condition) {

        CursorPagingResponse<ProblemListItem, Long> response =
                problemService.searchProblems(authUser.userId(), condition);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @GetMapping("/{problemId}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemDetail(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long problemId) {

        ProblemResponse response = problemService.getProblemDetail(authUser.userId(), problemId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @PutMapping("/{problemId}/bookmark")
    public ResponseEntity<Void> registerBookmark(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long problemId) {

        problemBookmarkService.registerBookmark(authUser.userId(), problemId);

        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{problemId}/bookmark")
    public ResponseEntity<Void> removeBookmark(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long problemId) {

        problemBookmarkService.removeBookmark(authUser.userId(), problemId);

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<RecommendedProblemResponse>> getRecommendedProblem(
            @AuthenticationPrincipal AuthUser authUser) {

        RecommendedProblemResponse response = problemService.getRecommendedProblem(authUser.userId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
