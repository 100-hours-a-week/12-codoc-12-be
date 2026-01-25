package _ganzi.codoc.chatbot.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.chatbot")
public record ChatbotProperties(Duration sessionTtl) {}
