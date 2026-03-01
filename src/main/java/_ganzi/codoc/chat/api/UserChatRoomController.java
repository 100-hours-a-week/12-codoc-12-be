package _ganzi.codoc.chat.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chat.dto.UserChatRoomListItem;
import _ganzi.codoc.chat.dto.UserChatUnreadStatusResponse;
import _ganzi.codoc.chat.service.ChatRoomService;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/user/chat-rooms")
@RestController
public class UserChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping
    public ResponseEntity<ApiResponse<CursorPagingResponse<UserChatRoomListItem, String>>>
            getUserChatRooms(
                    @AuthenticationPrincipal AuthUser authUser,
                    @RequestParam(required = false) String cursor,
                    @RequestParam(required = false) Integer limit) {
        CursorPagingResponse<UserChatRoomListItem, String> response =
                chatRoomService.getUserChatRooms(authUser.userId(), cursor, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<CursorPagingResponse<UserChatRoomListItem, String>>>
            searchUserChatRooms(
                    @AuthenticationPrincipal AuthUser authUser,
                    @RequestParam @Size(min = 1, max = 100) String keyword,
                    @RequestParam(required = false) String cursor,
                    @RequestParam(required = false) Integer limit) {
        CursorPagingResponse<UserChatRoomListItem, String> response =
                chatRoomService.searchUserChatRooms(authUser.userId(), keyword, cursor, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-status")
    public ResponseEntity<ApiResponse<UserChatUnreadStatusResponse>> getUserChatUnreadStatus(
            @AuthenticationPrincipal AuthUser authUser) {
        UserChatUnreadStatusResponse response =
                chatRoomService.getUserChatUnreadStatus(authUser.userId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
