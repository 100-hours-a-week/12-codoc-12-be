package _ganzi.codoc.ai.dto;

public record AiServerChatbotEvent<T>(String code, String message, T result) {}
