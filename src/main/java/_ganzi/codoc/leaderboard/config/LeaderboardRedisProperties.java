package _ganzi.codoc.leaderboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.leaderboard.redis")
public record LeaderboardRedisProperties(boolean writeEnabled, boolean readEnabled) {}
