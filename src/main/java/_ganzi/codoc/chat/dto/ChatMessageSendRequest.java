package _ganzi.codoc.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageSendRequest(@NotBlank @Size(max = 500) String content) {}
