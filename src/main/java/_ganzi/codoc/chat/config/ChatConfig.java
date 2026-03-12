package _ganzi.codoc.chat.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({ChatProperties.class, ChatWebSocketProperties.class})
public class ChatConfig {}
