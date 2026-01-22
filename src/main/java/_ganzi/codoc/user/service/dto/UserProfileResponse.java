package _ganzi.codoc.user.service.dto;

public record UserProfileResponse(
        String nickname, Integer avatarId, String avatarName, String avatarImageUrl) {}
