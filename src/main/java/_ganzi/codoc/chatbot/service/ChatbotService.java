package _ganzi.codoc.chatbot.service;

import _ganzi.codoc.ai.config.AiServerProperties;
import _ganzi.codoc.ai.dto.AiServerChatbotFinalEvent;
import _ganzi.codoc.ai.dto.AiServerChatbotFinalResult;
import _ganzi.codoc.ai.dto.AiServerChatbotSendRequest;
import _ganzi.codoc.ai.dto.AiServerErrorEvent;
import _ganzi.codoc.ai.infra.ChatbotClient;
import _ganzi.codoc.ai.util.AiServerResponseParser;
import _ganzi.codoc.chatbot.domain.ChatbotAttempt;
import _ganzi.codoc.chatbot.domain.ChatbotConversation;
import _ganzi.codoc.chatbot.dto.ChatbotConversationListCondition;
import _ganzi.codoc.chatbot.dto.ChatbotConversationListItem;
import _ganzi.codoc.chatbot.dto.ChatbotMessageSendRequest;
import _ganzi.codoc.chatbot.enums.ChatbotAttemptStatus;
import _ganzi.codoc.chatbot.enums.ChatbotParagraphType;
import _ganzi.codoc.chatbot.exception.ChatbotStreamEventException;
import _ganzi.codoc.chatbot.repository.ChatbotAttemptRepository;
import _ganzi.codoc.chatbot.repository.ChatbotConversationRepository;
import _ganzi.codoc.global.dto.CursorPagingResponse;
import _ganzi.codoc.global.util.CursorPagingUtils;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.exception.ProblemNotFoundException;
import _ganzi.codoc.problem.repository.ProblemRepository;
import _ganzi.codoc.submission.domain.ProblemSession;
import _ganzi.codoc.submission.exception.SessionRequiredException;
import _ganzi.codoc.submission.service.ProblemSessionService;
import _ganzi.codoc.user.domain.User;
import _ganzi.codoc.user.exception.UserNotFoundException;
import _ganzi.codoc.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    private final ProblemSessionService problemSessionService;
    private final AiServerProperties aiServerProperties;
    private final AiServerResponseParser responseParser;

    @Transactional
    public Flux<ServerSentEvent<String>> sendAndStream(
            Long userId, ChatbotMessageSendRequest request) {

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Problem problem =
                problemRepository.findById(request.problemId()).orElseThrow(ProblemNotFoundException::new);

        String userMessage = request.message();

        ProblemSession session = problemSessionService.requireActive(userId, request.problemId());
        if (session == null) {
            throw new SessionRequiredException();
        }

        ChatbotAttempt attempt = resolveAttempt(user, problem, session);

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
                .timeout(aiServerProperties.chatbotStreamTimeout())
                .doOnNext(event -> handleStreamEvent(chatbotConversation.getId(), event))
                .doOnError(error -> deleteConversationSilently(chatbotConversation.getId()));
    }

    public CursorPagingResponse<ChatbotConversationListItem, Long> getConversationList(
            Long userId, ChatbotConversationListCondition condition) {
        ProblemSession session = problemSessionService.requireActive(userId, condition.problemId());
        if (session == null) {
            throw new SessionRequiredException();
        }

        ChatbotAttempt attempt =
                chatbotAttemptRepository
                        .findFirstByProblemSessionIdAndStatusOrderByIdDesc(
                                session.getId(), ChatbotAttemptStatus.ACTIVE)
                        .orElse(null);

        if (attempt == null) {
            return new CursorPagingResponse<>(List.of(), null, false);
        }

        Pageable pageable = CursorPagingUtils.createPageable(condition.limit());
        List<ChatbotConversationListItem> items =
                chatbotConversationRepository
                        .findConversationListByAttemptId(attempt.getId(), condition.cursor(), pageable)
                        .stream()
                        .map(ChatbotConversationListItem::from)
                        .toList();

        return CursorPagingUtils.apply(
                items, condition.limit(), ChatbotConversationListItem::conversationId);
    }

    private ChatbotAttempt resolveAttempt(User user, Problem problem, ProblemSession session) {
        ChatbotAttempt attempt =
                chatbotAttemptRepository
                        .findFirstByProblemSessionIdAndStatusOrderByIdDesc(
                                session.getId(), ChatbotAttemptStatus.ACTIVE)
                        .orElse(null);

        if (attempt != null) {
            return attempt;
        }

        ChatbotAttempt newAttempt = ChatbotAttempt.create(user, problem, session);

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
