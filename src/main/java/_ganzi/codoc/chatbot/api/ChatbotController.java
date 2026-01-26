package _ganzi.codoc.chatbot.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendResponse;
import _ganzi.codoc.chatbot.service.ChatbotService;
import _ganzi.codoc.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/chatbot")
@RestController
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatbotMessageSendResponse>> sendMessage(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ChatbotMessageSendRequest request) {

        ChatbotMessageSendResponse response = chatbotService.sendMessage(authUser.userId(), request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
