package _ganzi.codoc.chatbot.api;

import _ganzi.codoc.auth.domain.AuthUser;
import _ganzi.codoc.chatbot.dto.ChatbotConversationListCondition;
import _ganzi.codoc.chatbot.dto.ChatbotConversationListItem;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.exception.ChatbotErrorCode;
import _ganzi.codoc.global.api.docs.ErrorCodes;
import _ganzi.codoc.global.dto.ApiResponse;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.global.exception.GlobalErrorCode;
import _ganzi.codoc.problem.exception.ProblemErrorCode;
import _ganzi.codoc.submission.exception.SubmissionErrorCode;
import _ganzi.codoc.user.exception.UserErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

@Tag(name = "Chatbot", description = "Chatbot messaging endpoints")
public interface ChatbotApi {

    @Operation(summary = "Send and stream chatbot message (WebFlux)")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_INPUT, SESSION_REQUIRED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN, CHATBOT_CONVERSATION_NO_PERMISSION"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "PROBLEM_NOT_FOUND, USER_NOT_FOUND"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "CHATBOT_SESSION_ALREADY_COMPLETED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "429",
                description = "CHATBOT_STREAM_RATE_LIMIT_EXCEEDED"),
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
            chatbot = {
                ChatbotErrorCode.CHATBOT_SESSION_ALREADY_COMPLETED,
                ChatbotErrorCode.CHATBOT_STREAM_RATE_LIMIT_EXCEEDED,
                ChatbotErrorCode.CHATBOT_STREAM_EVENT_FAILED
            },
            submission = {SubmissionErrorCode.SESSION_REQUIRED},
            problem = {ProblemErrorCode.PROBLEM_NOT_FOUND},
            user = {UserErrorCode.USER_NOT_FOUND})
    Flux<ServerSentEvent<String>> sendAndStream(AuthUser authUser, ChatbotMessageSendRequest request);

    @Operation(summary = "Resume and stream chatbot message by conversationId")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_INPUT, SESSION_REQUIRED"),
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
                responseCode = "409",
                description = "CHATBOT_CONVERSATION_NOT_RESUMABLE, CHATBOT_SESSION_ALREADY_COMPLETED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "429",
                description = "CHATBOT_STREAM_RATE_LIMIT_EXCEEDED"),
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
            chatbot = {
                ChatbotErrorCode.CHATBOT_CONVERSATION_NOT_FOUND,
                ChatbotErrorCode.CHATBOT_CONVERSATION_NO_PERMISSION,
                ChatbotErrorCode.CHATBOT_CONVERSATION_NOT_RESUMABLE,
                ChatbotErrorCode.CHATBOT_SESSION_ALREADY_COMPLETED,
                ChatbotErrorCode.CHATBOT_STREAM_RATE_LIMIT_EXCEEDED,
                ChatbotErrorCode.CHATBOT_STREAM_EVENT_FAILED
            },
            submission = {SubmissionErrorCode.SESSION_REQUIRED})
    Flux<ServerSentEvent<String>> resumeAndStream(AuthUser authUser, Long conversationId);

    @Operation(summary = "Stop chatbot stream by conversationId")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_INPUT"),
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
                responseCode = "409",
                description = "CHATBOT_CONVERSATION_NOT_PROCESSING"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "503",
                description = "CHATBOT_STREAM_CANCEL_FAILED"),
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
            chatbot = {
                ChatbotErrorCode.CHATBOT_CONVERSATION_NOT_FOUND,
                ChatbotErrorCode.CHATBOT_CONVERSATION_NO_PERMISSION,
                ChatbotErrorCode.CHATBOT_CONVERSATION_NOT_PROCESSING,
                ChatbotErrorCode.CHATBOT_STREAM_CANCEL_FAILED
            })
    ResponseEntity<Void> stopStream(AuthUser authUser, Long conversationId);

    @Operation(summary = "Get chatbot conversation history of current session by problem")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "INVALID_INPUT, SESSION_REQUIRED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "401",
                description = "AUTH_REQUIRED, UNAUTHORIZED"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "403",
                description = "FORBIDDEN"),
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
            submission = {SubmissionErrorCode.SESSION_REQUIRED})
    ResponseEntity<ApiResponse<CursorPagingResponse<ChatbotConversationListItem, Long>>>
            getConversationList(AuthUser authUser, ChatbotConversationListCondition condition);
}
