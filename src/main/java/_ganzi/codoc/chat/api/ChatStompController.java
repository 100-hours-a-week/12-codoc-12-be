package _ganzi.codoc.chat.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chat.dto.ChatMessageSendRequest;
import _ganzi.codoc.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatStompController {

    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/messages/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageSendRequest request,
            AuthUser authUser) {

        chatMessageService.sendMessage(authUser.userId(), roomId, request);
    }
}
