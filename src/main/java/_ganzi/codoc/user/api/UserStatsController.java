package _ganzi.codoc.user.api;

import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.user.service.UserStatsService;
import _ganzi.codoc.user.service.dto.UserStatsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserStatsController {

    private final UserStatsService userStatsService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats(
            @RequestHeader("X-USER-ID") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userStatsService.getUserStats(userId)));
    }
}
