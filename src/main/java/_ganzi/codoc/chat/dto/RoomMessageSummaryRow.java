package _ganzi.codoc.chat.dto;

import java.time.Instant;

public interface RoomMessageSummaryRow {

    Long getMessageId();

    Long getRoomId();

    String getContent();

    Instant getCreatedAt();
}
