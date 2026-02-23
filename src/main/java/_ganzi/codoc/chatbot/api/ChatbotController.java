package _ganzi.codoc.chatbot.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.service.ChatbotService;
import _ganzi.codoc.global.ratelimit.RateLimit;
import _ganzi.codoc.global.ratelimit.RateLimitApiType;
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

    @RateLimit(type = RateLimitApiType.CHATBOT_STREAM)
    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sendAndStream(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ChatbotMessageSendRequest request) {
        return chatbotService.sendAndStream(authUser.userId(), request);
    }

    @Override
    @DeleteMapping("/messages/stream")
    public ResponseEntity<Void> cancelStream(
            @AuthenticationPrincipal AuthUser authUser, @RequestParam Long conversationId) {
        chatbotService.cancelStream(authUser.userId(), conversationId);
        return ResponseEntity.noContent().build();
    }
}
