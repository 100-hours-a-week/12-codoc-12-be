package _ganzi.codoc.ai.api;

import _ganzi.codoc.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "AI", description = "AI server health endpoints")
public interface AiServerHealthCheckApi {

    @Operation(summary = "Check AI server health")
    ResponseEntity<ApiResponse<String>> healthCheck();
}
