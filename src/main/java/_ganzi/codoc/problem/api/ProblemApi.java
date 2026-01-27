package _ganzi.codoc.problem.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.problem.dto.ProblemListCondition;
import _ganzi.codoc.problem.dto.ProblemListItem;
import _ganzi.codoc.problem.dto.ProblemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Problem", description = "Problem browsing endpoints")
public interface ProblemApi {

    @Operation(summary = "Get problem list")
    ResponseEntity<ApiResponse<CursorPagingResponse<ProblemListItem, Long>>> getProblemList(
            AuthUser authUser, ProblemListCondition condition);

    @Operation(summary = "Search problems")
    ResponseEntity<ApiResponse<CursorPagingResponse<ProblemListItem, Long>>> searchProblems(
            AuthUser authUser, ProblemListCondition condition);

    @Operation(summary = "Get problem detail")
    ResponseEntity<ApiResponse<ProblemResponse>> getProblemDetail(AuthUser authUser, Long problemId);

    @Operation(summary = "Register bookmark")
    ResponseEntity<Void> registerBookmark(AuthUser authUser, Long problemId);

    @Operation(summary = "Remove bookmark")
    ResponseEntity<Void> removeBookmark(AuthUser authUser, Long problemId);
}
