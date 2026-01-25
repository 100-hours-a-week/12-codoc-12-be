package _ganzi.codoc.ai.service;

import _ganzi.codoc.ai.infra.HealthCheckClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AiServerHealthCheckService {

    private final HealthCheckClient healthCheckClient;

    public String healthCheck() {
        return healthCheckClient.requestHealthCheck();
    }
}
