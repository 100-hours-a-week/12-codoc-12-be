package _ganzi.codoc.user.api.dto;

import _ganzi.codoc.user.api.validation.Nickname;

public record UserProfileUpdateRequest(@Nickname String nickname, Integer avatarId) {}
