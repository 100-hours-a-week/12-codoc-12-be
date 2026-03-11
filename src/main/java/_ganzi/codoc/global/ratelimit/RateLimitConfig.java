package _ganzi.codoc.global.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@EnableConfigurationProperties(RateLimitProperties.class)
@Configuration
public class RateLimitConfig {

    private static final Duration additionalBufferTime = Duration.ofSeconds(5);

    @Bean
    public ProxyManager<String> rateLimitProxyManager(
            StatefulRedisConnection<String, byte[]> binaryRedisConnection) {
        return Bucket4jLettuce.casBasedBuilder(binaryRedisConnection)
                .expirationAfterWrite(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(additionalBufferTime)
                )
                .build();
    }
}
