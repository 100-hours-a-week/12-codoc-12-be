package _ganzi.codoc.problem.config;

import _ganzi.codoc.problem.cache.RedisProblemContentCacheInvalidationSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@ConditionalOnProperty(
        prefix = "app.cache.problem-content.invalidation",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(ProblemContentCacheInvalidationProperties.class)
@Configuration
public class ProblemContentCacheInvalidationRedisConfig {

    @Bean
    public RedisMessageListenerContainer problemContentCacheInvalidationRedisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            RedisProblemContentCacheInvalidationSubscriber subscriber,
            ProblemContentCacheInvalidationProperties properties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(subscriber, ChannelTopic.of(properties.channel()));
        return container;
    }
}
