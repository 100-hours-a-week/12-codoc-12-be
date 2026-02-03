package _ganzi.codoc.auth.api.dto;

public record DevAuthResponse(Long userId, String accessToken, String tokenType, long expiresIn) {}
