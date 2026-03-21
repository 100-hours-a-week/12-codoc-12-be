package _ganzi.codoc.custom.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.custom.dto.*;
import _ganzi.codoc.custom.infra.CustomProblemStorageService;
import _ganzi.codoc.custom.service.CustomProblemService;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.ratelimit.RateLimit;
import _ganzi.codoc.global.ratelimit.RateLimitApiType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/custom-problems")
@RestController
public class CustomProblemController {

    private final CustomProblemStorageService customProblemStorageService;
    private final CustomProblemService customProblemService;

    @PostMapping("/upload-urls")
    public ResponseEntity<ApiResponse<CustomProblemUploadUrlsResponse>> issueUploadUrls(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CustomProblemUploadUrlsRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        customProblemStorageService.issueUploadUrls(authUser.userId(), request)));
    }

    @RateLimit(type = RateLimitApiType.CUSTOM_PROBLEM_GENERATE)
    @PostMapping
    public ResponseEntity<ApiResponse<CustomProblemCreateResponse>> createCustomProblem(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody CustomProblemCreateRequest request) {
        return ResponseEntity.accepted()
                .body(
                        ApiResponse.success(
                                customProblemService.createCustomProblem(authUser.userId(), request)));
    }

    @GetMapping("/{customProblemId}")
    public ResponseEntity<ApiResponse<CustomProblemDetailResponse>> getCustomProblemDetail(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long customProblemId) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        customProblemService.getCustomProblemDetail(authUser.userId(), customProblemId)));
    }

    @DeleteMapping("/{customProblemId}")
    public ResponseEntity<Void> deleteCustomProblem(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long customProblemId) {
        customProblemService.deleteCustomProblem(authUser.userId(), customProblemId);
        return ResponseEntity.noContent().build();
    }
}
