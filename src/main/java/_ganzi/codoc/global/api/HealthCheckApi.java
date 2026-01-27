package _ganzi.codoc.global.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Health", description = "Service health endpoints")
public interface HealthCheckApi {

    @Operation(summary = "Check service health")
    String healthCheck();
}
