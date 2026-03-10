package _ganzi.codoc.global.config;

import _ganzi.codoc.global.constants.CacheNames;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@EnableConfigurationProperties(AppCacheProperties.class)
@EnableCaching
@Configuration
public class CacheConfig {

    private final AppCacheProperties properties;

    @Bean(name = CacheNames.CAFFEINE_CACHE_MANAGER)
    public CacheManager caffeineCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(
                List.of(
                        buildCaffeineCache(CacheNames.PROBLEM_DETAIL),
                        buildCaffeineCache(CacheNames.PROBLEM_SUMMARY_CARDS),
                        buildCaffeineCache(CacheNames.PROBLEM_QUIZZES),
                        buildCaffeineNegativeCache(CacheNames.PROBLEM_NEGATIVE)));
        return cacheManager;
    }

    private CaffeineCache buildCaffeineCache(String cacheName) {
        String spec = properties.resolveSpec(cacheName);
        return createCaffeineCache(cacheName, spec);
    }

    private CaffeineCache buildCaffeineNegativeCache(String cacheName) {
        String spec = properties.resolveNegativeSpec(cacheName);
        return createCaffeineCache(cacheName, spec);
    }

    private CaffeineCache createCaffeineCache(String cacheName, String spec) {
        return new CaffeineCache(cacheName, Caffeine.from(spec).build(), false);
    }
}
