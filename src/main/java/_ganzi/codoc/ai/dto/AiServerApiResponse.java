package _ganzi.codoc.ai.dto;

import lombok.Builder;

@Builder
public record AiServerApiResponse<T>(String code, String message, T data) {

    public static <T> AiServerApiResponse<T> of(String code, String message, T data) {
        return AiServerApiResponse.<T>builder().code(code).message(message).data(data).build();
    }
}
