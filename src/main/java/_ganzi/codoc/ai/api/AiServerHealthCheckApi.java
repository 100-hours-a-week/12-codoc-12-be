package _ganzi.codoc.ai.api;

import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "AI", description = "AI server health endpoints")
public interface AiServerHealthCheckApi {

    @Operation(summary = "Check AI server health")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(global = {GlobalErrorCode.INTERNAL_SERVER_ERROR})
    ResponseEntity<ApiResponse<String>> healthCheck();
}
