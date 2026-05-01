package _ganzi.codoc.problem.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(ProblemContentCacheProperties.class)
@Configuration
public class ProblemCacheConfig {

    public static final String PROBLEM_CONTENT = "problem-content";
    public static final String PROBLEM_NULL = "problem-null";

    @Bean
    public CacheManager problemCacheManager(ProblemContentCacheProperties properties) {
        CaffeineCache contentCache = new CaffeineCache(
                PROBLEM_CONTENT,
                Caffeine.from(properties.problemContent()).build());

        CaffeineCache nullCache = new CaffeineCache(
                PROBLEM_NULL,
                Caffeine.from(properties.problemNull()).build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(contentCache, nullCache));
        return manager;
    }
}
