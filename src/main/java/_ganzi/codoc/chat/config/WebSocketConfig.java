package _ganzi.codoc.chat.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String frontendBaseUrl;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    private final ChatRoomSubscriptionInterceptor chatRoomSubscriptionInterceptor;
    private final StompAuthUserArgumentResolver stompAuthUserArgumentResolver;

    public WebSocketConfig(
            @Value("${app.frontend.base-url}") String frontendBaseUrl,
            WebSocketAuthInterceptor webSocketAuthInterceptor,
            ChatRoomSubscriptionInterceptor chatRoomSubscriptionInterceptor,
            StompAuthUserArgumentResolver stompAuthUserArgumentResolver) {
        this.frontendBaseUrl = frontendBaseUrl;
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
        this.chatRoomSubscriptionInterceptor = chatRoomSubscriptionInterceptor;
        this.stompAuthUserArgumentResolver = stompAuthUserArgumentResolver;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat").setAllowedOrigins(frontendBaseUrl).withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor, chatRoomSubscriptionInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(stompAuthUserArgumentResolver);
    }
}
