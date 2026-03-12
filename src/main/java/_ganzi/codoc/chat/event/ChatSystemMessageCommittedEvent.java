package _ganzi.codoc.chat.event;

import _ganzi.codoc.chat.dto.ChatMessageBroadcast;

public record ChatSystemMessageCommittedEvent(Long roomId, ChatMessageBroadcast roomMessage) {}
