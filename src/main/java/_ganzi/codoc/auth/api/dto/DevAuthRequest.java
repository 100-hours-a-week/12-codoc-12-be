package _ganzi.codoc.auth.api.dto;

public record DevAuthRequest(Long userId, String nickname, Long accessTokenTtlMinutes) {}
