package _ganzi.codoc.global.config;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cache.caffeine")
public record AppCacheProperties(@NotBlank String defaultSpec, Map<String, String> specs) {

    public AppCacheProperties {
        specs = specs == null ? Map.of() : Map.copyOf(specs);
    }

    public String resolveSpec(String cacheName) {
        return specs.getOrDefault(cacheName, defaultSpec);
    }
}
