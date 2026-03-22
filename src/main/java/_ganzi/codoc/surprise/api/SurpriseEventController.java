package _ganzi.codoc.surprise.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.surprise.dto.SurpriseQuizSubmitRequest;
import _ganzi.codoc.surprise.dto.SurpriseQuizSubmitResponse;
import _ganzi.codoc.surprise.dto.SurpriseQuizViewResponse;
import _ganzi.codoc.surprise.service.SurpriseQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/events/surprise")
@RestController
public class SurpriseEventController {

    private final SurpriseQuizService surpriseQuizService;

    @GetMapping("/quiz")
    public ResponseEntity<ApiResponse<SurpriseQuizViewResponse>> getCurrentQuiz(
            @AuthenticationPrincipal AuthUser authUser) {
        SurpriseQuizViewResponse response = surpriseQuizService.getCurrentQuiz(authUser.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{eventId}/quiz")
    public ResponseEntity<ApiResponse<SurpriseQuizViewResponse>> getQuiz(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long eventId) {
        SurpriseQuizViewResponse response = surpriseQuizService.getQuiz(authUser.userId(), eventId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/quiz/submissions")
    public ResponseEntity<ApiResponse<SurpriseQuizSubmitResponse>> submitCurrentQuiz(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody SurpriseQuizSubmitRequest request) {
        SurpriseQuizSubmitResponse response =
                surpriseQuizService.submitCurrentQuiz(authUser.userId(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{eventId}/quiz/submissions")
    public ResponseEntity<ApiResponse<SurpriseQuizSubmitResponse>> submitQuiz(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long eventId,
            @Valid @RequestBody SurpriseQuizSubmitRequest request) {
        SurpriseQuizSubmitResponse response =
                surpriseQuizService.submitQuiz(authUser.userId(), eventId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
