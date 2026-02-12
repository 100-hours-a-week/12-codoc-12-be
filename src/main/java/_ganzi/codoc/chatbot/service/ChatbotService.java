package _ganzi.codoc.chatbot.service;

import _ganzi.codoc.ai.dto.AiServerChatbotFinalEvent;
import _ganzi.codoc.ai.dto.AiServerChatbotFinalResult;
import _ganzi.codoc.ai.dto.AiServerChatbotSendRequest;
import _ganzi.codoc.ai.dto.AiServerErrorEvent;
import _ganzi.codoc.ai.infra.ChatbotClient;
import _ganzi.codoc.ai.util.AiServerResponseParser;
import _ganzi.codoc.chatbot.config.ChatbotProperties;
import _ganzi.codoc.chatbot.domain.ChatbotAttempt;
import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.enums.ChatbotAttemptStatus;
import _ganzi.codoc.chatbot.enums.ChatbotParagraphType;
import _ganzi.codoc.chatbot.exception.ChatbotStreamEventException;
import _ganzi.codoc.chatbot.repository.ChatbotAttemptRepository;
import _ganzi.codoc.chatbot.repository.ChatbotConversationRepository;
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

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatbotService {

    private static final String EVENT_FINAL = "final";
    private static final String EVENT_ERROR = "error";
    private static final String CODE_SUCCESS = "SUCCESS";

    private final ChatbotClient chatbotClient;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final ChatbotConversationRepository chatbotConversationRepository;
    private final ChatbotAttemptRepository chatbotAttemptRepository;
    private final ChatbotProperties chatbotProperties;
    private final AiServerResponseParser responseParser;

    @Transactional
    public Flux<ServerSentEvent<String>> sendAndStream(
            Long userId, ChatbotMessageSendRequest request) {

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

        return chatbotClient
                .streamMessage(aiServerRequest)
                .doOnNext(event -> handleStreamEvent(chatbotConversation.getId(), event))
                .doOnError(error -> deleteConversationSilently(chatbotConversation.getId()));
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

    private void handleStreamEvent(Long conversationId, ServerSentEvent<String> event) {
        String eventName = event.event();
        String data = event.data();

        if (EVENT_FINAL.equals(eventName)) {
            handleFinalSuccessEvent(conversationId, data);
            return;
        }

        if (EVENT_ERROR.equals(eventName)) {
            handleErrorEvent(conversationId, data);
        }
    }

    private void handleFinalSuccessEvent(Long conversationId, String data) {
        if (data == null || data.isBlank()) {
            deleteAndThrow(conversationId);
        }

        AiServerChatbotFinalEvent finalEvent = responseParser.parseChatbotFinalEvent(data);
        if (finalEvent == null || !CODE_SUCCESS.equals(finalEvent.code())) {
            deleteAndThrow(conversationId);
        }

        if (finalEvent.result() == null) {
            deleteAndThrow(conversationId);
        }

        persistFinalEvent(conversationId, finalEvent);
    }

    private void handleErrorEvent(Long conversationId, String data) {
        AiServerErrorEvent errorEvent = responseParser.parseErrorEvent(data);
        if (errorEvent == null) {
            deleteAndThrow(conversationId);
        }

        deleteAndThrow(conversationId);
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
