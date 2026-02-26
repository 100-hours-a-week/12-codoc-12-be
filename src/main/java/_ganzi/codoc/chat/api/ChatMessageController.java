package _ganzi.codoc.chat.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chat.dto.ChatMessageListItem;
import _ganzi.codoc.chat.service.ChatMessageService;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms/{roomId}/messages")
@RestController
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPagingResponse<ChatMessageListItem, String>>> getMessages(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable @Positive(message = "roomId는 1 이상의 숫자여야 합니다.") Long roomId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {
        CursorPagingResponse<ChatMessageListItem, String> response =
                chatMessageService.getMessages(authUser.userId(), roomId, cursor, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
