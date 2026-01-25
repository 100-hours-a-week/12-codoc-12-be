package _ganzi.codoc.ai.api;

import _ganzi.codoc.ai.service.HealthCheckService;
import _ganzi.codoc.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/ai-health")
@RestController
public class HealthCheckController {

    private final HealthCheckService healthCheckService;

    @GetMapping
    public ResponseEntity<ApiResponse<String>> healthCheck() {

        String response = healthCheckService.healthCheck();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
