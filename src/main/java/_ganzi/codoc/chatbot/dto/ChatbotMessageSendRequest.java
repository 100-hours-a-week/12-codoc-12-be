package _ganzi.codoc.chatbot.dto;

import _ganzi.codoc.chatbot.enums.ChatbotMessageType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatbotMessageSendRequest(
        @NotNull Long problemId,
        @NotBlank String message,
        @JsonProperty("message_type") @JsonAlias("messageType") ChatbotMessageType messageType) {

    public ChatbotMessageType resolveMessageType() {
        return messageType == null ? ChatbotMessageType.ANSWER : messageType;
    }
}
