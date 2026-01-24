package _ganzi.codoc.user.service;

import _ganzi.codoc.user.repository.AvatarRepository;
import _ganzi.codoc.user.service.dto.UserAvatarListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarService {

    private final AvatarRepository avatarRepository;

    public UserAvatarListResponse getAvatarList() {
        List<UserAvatarListResponse.AvatarItem> avatars =
                avatarRepository.findAll().stream()
                        .map(
                                avatar ->
                                        new UserAvatarListResponse.AvatarItem(avatar.getId(), avatar.getImageUrl()))
                        .toList();
        return new UserAvatarListResponse(avatars);
    }
}
