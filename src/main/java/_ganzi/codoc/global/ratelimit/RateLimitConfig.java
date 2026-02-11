package _ganzi.codoc.global.ratelimit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(RateLimitProperties.class)
@Configuration
public class RateLimitConfig {}
