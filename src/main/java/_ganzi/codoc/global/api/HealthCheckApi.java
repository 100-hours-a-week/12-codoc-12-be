package _ganzi.codoc.global.api;

import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "Service health endpoints")
public interface HealthCheckApi {

    @Operation(summary = "Check service health")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(global = {GlobalErrorCode.INTERNAL_SERVER_ERROR})
    String healthCheck();
}
