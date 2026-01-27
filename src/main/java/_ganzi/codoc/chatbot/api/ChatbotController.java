package _ganzi.codoc.chatbot.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendResponse;
import _ganzi.codoc.chatbot.service.ChatbotService;
import _ganzi.codoc.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
@RestController
public class ChatbotController implements ChatbotApi {

    private final ChatbotService chatbotService;

    @Override
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatbotMessageSendResponse>> sendMessage(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ChatbotMessageSendRequest request) {

        ChatbotMessageSendResponse response = chatbotService.sendMessage(authUser.userId(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @GetMapping(
            value = "/messages/{conversationId}/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamMessage(@PathVariable Long conversationId) {
        return chatbotService.streamMessage(conversationId);
    }
}
