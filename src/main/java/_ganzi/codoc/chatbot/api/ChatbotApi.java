package _ganzi.codoc.chatbot.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendResponse;
import _ganzi.codoc.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

@Tag(name = "Chatbot", description = "Chatbot messaging endpoints")
public interface ChatbotApi {

    @Operation(summary = "Send chatbot message")
    ResponseEntity<ApiResponse<ChatbotMessageSendResponse>> sendMessage(
            AuthUser authUser, ChatbotMessageSendRequest request);

    @Operation(summary = "Stream chatbot message")
    Flux<ServerSentEvent<String>> streamMessage(Long conversationId);
}
