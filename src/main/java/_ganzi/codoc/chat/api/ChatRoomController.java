package _ganzi.codoc.chat.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chat.dto.ChatRoomCreateRequest;
import _ganzi.codoc.chat.dto.ChatRoomCreateResponse;
import _ganzi.codoc.chat.dto.ChatRoomJoinRequest;
import _ganzi.codoc.chat.dto.ChatRoomListItem;
import _ganzi.codoc.chat.service.ChatRoomService;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/chat-rooms")
@RestController
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createChatRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody ChatRoomCreateRequest request) {
        ChatRoomCreateResponse response = chatRoomService.createChatRoom(authUser.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<Void> joinChatRoom(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long roomId,
            @RequestBody(required = false) ChatRoomJoinRequest request) {
        chatRoomService.joinChatRoom(authUser.userId(), roomId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<Void> leaveChatRoom(
            @AuthenticationPrincipal AuthUser authUser, @PathVariable Long roomId) {
        chatRoomService.leaveChatRoom(authUser.userId(), roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPagingResponse<ChatRoomListItem, String>>>
            searchChatRooms(
                    @RequestParam @Size(min = 1, max = 100) String keyword,
                    @RequestParam(required = false) String cursor,
                    @RequestParam(required = false) Integer limit) {
        CursorPagingResponse<ChatRoomListItem, String> response =
                chatRoomService.searchAllChatRooms(keyword, cursor, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
