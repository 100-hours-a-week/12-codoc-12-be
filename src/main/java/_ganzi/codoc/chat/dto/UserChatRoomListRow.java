package _ganzi.codoc.chat.dto;

import java.time.Instant;

public interface UserChatRoomListRow {

    Long getParticipantId();

    Long getRoomId();

    String getTitle();

    String getLastMessagePreview();

    Instant getLastMessageAt();
}
