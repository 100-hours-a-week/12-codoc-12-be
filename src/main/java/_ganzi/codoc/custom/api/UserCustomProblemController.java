package _ganzi.codoc.custom.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.custom.dto.CustomProblemListItem;
import _ganzi.codoc.custom.service.CustomProblemService;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/user/custom-problems")
@RestController
public class UserCustomProblemController {

    private final CustomProblemService customProblemService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPagingResponse<CustomProblemListItem, String>>>
            getCustomProblems(
                    @AuthenticationPrincipal AuthUser authUser,
                    @RequestParam(required = false) String cursor,
                    @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        customProblemService.getCustomProblems(authUser.userId(), cursor, limit)));
    }
}
