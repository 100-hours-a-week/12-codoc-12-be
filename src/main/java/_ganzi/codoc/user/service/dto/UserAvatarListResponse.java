package _ganzi.codoc.user.service.dto;

import java.util.List;

public record UserAvatarListResponse(List<AvatarItem> avatars) {

    public record AvatarItem(Integer avatarId, String url) {}
}
