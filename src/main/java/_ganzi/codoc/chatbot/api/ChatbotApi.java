package _ganzi.codoc.chatbot.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendResponse;
import _ganzi.codoc.chatbot.exception.ChatbotErrorCode;
import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import _ganzi.codoc.problem.exception.ProblemErrorCode;
import _ganzi.codoc.user.exception.UserErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

@Tag(name = "Chatbot", description = "Chatbot messaging endpoints")
public interface ChatbotApi {

    @Operation(summary = "Send chatbot message")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_INPUT"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "PROBLEM_NOT_FOUND, USER_NOT_FOUND"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.INVALID_INPUT,
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            problem = {ProblemErrorCode.PROBLEM_NOT_FOUND},
            user = {UserErrorCode.USER_NOT_FOUND})
    ResponseEntity<ApiResponse<ChatbotMessageSendResponse>> sendMessage(
            AuthUser authUser, ChatbotMessageSendRequest request);

    @Operation(summary = "Stream chatbot message")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN, CHATBOT_CONVERSATION_NO_PERMISSION"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "CHATBOT_CONVERSATION_NOT_FOUND"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "INTERNAL_SERVER_ERROR")
    })
    @ErrorCodes(
            global = {
                GlobalErrorCode.AUTH_REQUIRED,
                GlobalErrorCode.UNAUTHORIZED,
                GlobalErrorCode.FORBIDDEN,
                GlobalErrorCode.INTERNAL_SERVER_ERROR
            },
            chatbot = {
                ChatbotErrorCode.CHATBOT_CONVERSATION_NOT_FOUND,
                ChatbotErrorCode.CHATBOT_CONVERSATION_NO_PERMISSION
            })
    Flux<ServerSentEvent<String>> streamMessage(AuthUser authUser, Long conversationId);
}
