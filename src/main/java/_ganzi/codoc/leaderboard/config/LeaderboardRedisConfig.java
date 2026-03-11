package _ganzi.codoc.leaderboard.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LeaderboardRedisProperties.class)
public class LeaderboardRedisConfig {}
