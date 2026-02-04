package _ganzi.codoc.chatbot.service;

import _ganzi.codoc.ai.dto.AiServerApiResponse;
import _ganzi.codoc.ai.dto.AiServerChatbotFinalEvent;
import _ganzi.codoc.ai.dto.AiServerChatbotFinalResult;
import _ganzi.codoc.ai.dto.AiServerChatbotSendRequest;
import _ganzi.codoc.ai.dto.AiServerChatbotSendResponse;
import _ganzi.codoc.ai.infra.ChatbotClient;
import _ganzi.codoc.chatbot.config.ChatbotProperties;
import _ganzi.codoc.chatbot.domain.ChatbotAttempt;
import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendResponse;
import _ganzi.codoc.chatbot.enums.ChatbotAttemptStatus;
import _ganzi.codoc.chatbot.enums.ChatbotParagraphType;
import _ganzi.codoc.chatbot.exception.ChatbotConversationNoPermissionException;
import _ganzi.codoc.chatbot.exception.ChatbotConversationNotFoundException;
import _ganzi.codoc.chatbot.exception.ChatbotStreamEventException;
import _ganzi.codoc.chatbot.repository.ChatbotAttemptRepository;
import _ganzi.codoc.chatbot.repository.ChatbotConversationRepository;
import _ganzi.codoc.global.util.JsonUtils;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatbotService {

    private static final String EVENT_FINAL = "final";
    private static final String EVENT_ERROR = "error";
    private static final String EVENT_STATUS = "status";
    private static final String EVENT_TOKEN = "token";
    private static final String CODE_SUCCESS = "SUCCESS";

    private final ChatbotClient chatbotClient;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final ChatbotConversationRepository chatbotConversationRepository;
    private final ChatbotAttemptRepository chatbotAttemptRepository;
    private final ChatbotProperties chatbotProperties;
    private final JsonMapper jsonMapper;

    @Transactional
    public ChatbotMessageSendResponse sendMessage(Long userId, ChatbotMessageSendRequest request) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Problem problem =
                problemRepository.findById(request.problemId()).orElseThrow(ProblemNotFoundException::new);

        String userMessage = request.message();

        ChatbotAttempt attempt = resolveAttempt(user, problem);

        ChatbotConversation chatbotConversation =
                chatbotConversationRepository.save(
                        ChatbotConversation.create(attempt, userMessage, attempt.getCurrentParagraphType()));

        AiServerChatbotSendRequest aiServerRequest =
                AiServerChatbotSendRequest.of(
                        userId,
                        request.problemId(),
                        chatbotConversation.getId(),
                        userMessage,
                        user.getInitLevel(),
                        attempt.getCurrentParagraphType());

        AiServerApiResponse<AiServerChatbotSendResponse> aiServerResponse =
                chatbotClient.sendMessage(aiServerRequest);

        AiServerChatbotSendResponse sendResponse =
                validateSendResponse(chatbotConversation.getId(), aiServerResponse);

        return ChatbotMessageSendResponse.of(chatbotConversation.getId(), sendResponse.status());
    }

    private ChatbotAttempt resolveAttempt(User user, Problem problem) {
        ChatbotAttempt attempt =
                chatbotAttemptRepository
                        .findFirstByUserIdAndProblemIdAndStatusOrderByIdDesc(
                                user.getId(), problem.getId(), ChatbotAttemptStatus.ACTIVE)
                        .orElse(null);

        if (attempt != null) {
            attempt.expireIfNeeded(Instant.now());

            if (attempt.getStatus() == ChatbotAttemptStatus.ACTIVE) {
                return attempt;
            }
        }

        ChatbotAttempt newAttempt =
                ChatbotAttempt.create(user, problem, chatbotProperties.sessionTtl());

        return chatbotAttemptRepository.save(newAttempt);
    }

    private AiServerChatbotSendResponse validateSendResponse(
            Long conversationId, AiServerApiResponse<AiServerChatbotSendResponse> aiServerResponse) {

        if (aiServerResponse == null || aiServerResponse.data() == null) {
            deleteAndThrow(conversationId);
        }

        return aiServerResponse.data();
    }

    public Flux<ServerSentEvent<String>> streamMessage(Long userId, Long conversationId) {
        return Mono.fromCallable(
                        () ->
                                chatbotConversationRepository
                                        .findWithAttemptAndUserById(conversationId)
                                        .orElseThrow(ChatbotConversationNotFoundException::new))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(
                        conversation -> {
                            if (!userId.equals(conversation.getAttempt().getUser().getId())) {
                                return Flux.error(new ChatbotConversationNoPermissionException());
                            }

                            return chatbotClient
                                    .streamMessage(conversationId)
                                    .doOnNext(event -> handleStreamEvent(conversationId, event))
                                    .doOnError(error -> deleteConversationSilently(conversationId));
                        });
    }

    private void handleStreamEvent(Long conversationId, ServerSentEvent<String> event) {
        String eventName = event.event();
        String data = event.data();

        if (EVENT_FINAL.equals(eventName)) {
            handleFinalSuccessEvent(conversationId, data);
            return;
        }

        if (EVENT_STATUS.equals(eventName) || EVENT_TOKEN.equals(eventName)) {
            validateNonFinalEventOrThrow(conversationId, eventName, data);
            return;
        }

        deleteAndThrow(conversationId);
    }

    private void handleFinalSuccessEvent(Long conversationId, String data) {
        if (data == null || data.isBlank()) {
            deleteAndThrow(conversationId);
        }

        String code = extractCode(data);
        if (!CODE_SUCCESS.equals(code)) {
            deleteAndThrow(conversationId);
        }

        AiServerChatbotFinalEvent finalEvent =
                JsonUtils.parseJson(jsonMapper, data, AiServerChatbotFinalEvent.class);

        if (finalEvent == null || finalEvent.result() == null) {
            deleteAndThrow(conversationId);
        }

        persistFinalEvent(conversationId, finalEvent);
    }

    private void validateNonFinalEventOrThrow(Long conversationId, String eventName, String data) {
        String code = extractCode(data);

        if (code == null) {
            return;
        }

        if (!CODE_SUCCESS.equals(code)) {
            deleteAndThrow(conversationId);
        }
    }

    private String extractCode(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }

        JsonNode root = JsonUtils.parseJson(jsonMapper, data);
        if (root == null) {
            return null;
        }

        JsonNode code = root.get("code");
        return code != null ? code.asString() : null;
    }

    private void persistFinalEvent(Long conversationId, AiServerChatbotFinalEvent finalEvent) {
        AiServerChatbotFinalResult result = finalEvent.result();
        String aiMessage = result.aiMessage();
        Boolean isCorrect = result.isCorrect();
        ChatbotParagraphType paragraphType =
                StringUtils.hasText(result.paragraphType())
                        ? ChatbotParagraphType.valueOf(result.paragraphType())
                        : null;

        ChatbotConversation conversation =
                chatbotConversationRepository.findByIdWithAttempt(conversationId).orElse(null);
        if (conversation == null) {
            return;
        }

        if (aiMessage != null) {
            conversation.recordAiResponse(aiMessage, Boolean.TRUE.equals(isCorrect));
        }

        if (paragraphType != null) {
            ChatbotAttempt attempt = conversation.getAttempt();
            attempt.advanceToNextParagraph();
            chatbotAttemptRepository.save(attempt);
        }

        chatbotConversationRepository.save(conversation);
    }

    private void deleteConversationSilently(Long conversationId) {
        chatbotConversationRepository.deleteById(conversationId);
    }

    private void deleteAndThrow(Long conversationId) {
        deleteConversationSilently(conversationId);
        throw new ChatbotStreamEventException();
    }
}
