package _ganzi.codoc.chat.dto;

import jakarta.validation.constraints.Positive;

public record ChatMessageReadAckRequest(@Positive long lastReadMessageId) {}
