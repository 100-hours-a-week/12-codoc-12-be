package _ganzi.codoc.auth.service.dto;

public record TokenPairResponse(
        String accessToken, String refreshToken, String tokenType, long expiresIn) {}
