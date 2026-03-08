package _ganzi.codoc.global.config;

import _ganzi.codoc.global.constants.CacheNames;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(AppCacheProperties.class)
@EnableCaching
@Configuration
public class CacheConfig {

    @Bean(name = CacheNames.CAFFEINE_CACHE_MANAGER)
    public CacheManager caffeineCacheManager(AppCacheProperties properties) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(
                List.of(
                        buildCache(CacheNames.PROBLEM_DETAIL, properties, false),
                        buildCache(CacheNames.PROBLEM_SUMMARY_CARDS, properties, false),
                        buildCache(CacheNames.PROBLEM_QUIZZES, properties, false)));
        return cacheManager;
    }

    private CaffeineCache buildCache(
            String cacheName, AppCacheProperties properties, boolean allowNullValues) {
        return new CaffeineCache(
                cacheName, Caffeine.from(properties.resolveSpec(cacheName)).build(), allowNullValues);
    }
}
