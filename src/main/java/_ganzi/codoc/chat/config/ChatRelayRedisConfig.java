package _ganzi.codoc.chat.config;

import _ganzi.codoc.chat.relay.RedisChatRelaySubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@ConditionalOnProperty(
        prefix = "app.chat.ws",
        name = "relay-enabled",
        havingValue = "true",
        matchIfMissing = true)
@Configuration
public class ChatRelayRedisConfig {

    @Bean
    public RedisMessageListenerContainer chatRelayRedisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            RedisChatRelaySubscriber redisChatRelaySubscriber,
            ChatWebSocketProperties properties) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(
                redisChatRelaySubscriber, ChannelTopic.of(properties.relayChannel()));
        return container;
    }
}
