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
import _ganzi.codoc.chatbot.exception.ChatbotConversationNoPermissionException;
import _ganzi.codoc.chatbot.exception.ChatbotConversationNotFoundException;
import _ganzi.codoc.chatbot.repository.ChatbotAttemptRepository;
import _ganzi.codoc.chatbot.repository.ChatbotConversationRepository;
import _ganzi.codoc.global.util.JsonUtils;
import _ganzi.codoc.problem.domain.Problem;
import _ganzi.codoc.problem.enums.ParagraphType;
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
import tools.jackson.databind.json.JsonMapper;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatbotService {

    private static final String EVENT_FINAL = "final";

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
                        ChatbotConversation.create(attempt, userMessage, attempt.getCurrentNode()));

        AiServerChatbotSendRequest aiServerRequest =
                AiServerChatbotSendRequest.of(
                        userId,
                        request.problemId(),
                        chatbotConversation.getId(),
                        userMessage,
                        attempt.getCurrentNode());

        AiServerApiResponse<AiServerChatbotSendResponse> aiServerResponse =
                chatbotClient.sendMessage(aiServerRequest);

        AiServerChatbotSendResponse sendResponse = validateSendResponse(aiServerResponse);

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
            AiServerApiResponse<AiServerChatbotSendResponse> aiServerResponse) {

        if (aiServerResponse == null || aiServerResponse.data() == null) {
            throw new IllegalStateException("Ai server response is empty.");
        }

        return aiServerResponse.data();
    }

    public Flux<ServerSentEvent<String>> streamMessage(Long userId, Long conversationId) {

        ChatbotConversation conversation =
                chatbotConversationRepository
                        .findById(conversationId)
                        .orElseThrow(ChatbotConversationNotFoundException::new);

        if (!userId.equals(conversation.getAttempt().getUser().getId())) {
            throw new ChatbotConversationNoPermissionException();
        }

        return chatbotClient
                .streamMessage(conversationId)
                .doOnNext(event -> handleFinalEvent(conversationId, event))
                .doOnError(error -> deleteConversationSilently(conversationId));
    }

    private void handleFinalEvent(Long conversationId, ServerSentEvent<String> event) {
        String eventName = event.event();

        if (!EVENT_FINAL.equals(eventName)) {
            return;
        }

        String data = event.data();

        if (data == null || data.isBlank()) {
            return;
        }

        persistFinalEvent(conversationId, data);
    }

    private void persistFinalEvent(Long conversationId, String data) {
        AiServerChatbotFinalEvent finalEvent =
                JsonUtils.parseJson(jsonMapper, data, AiServerChatbotFinalEvent.class);

        if (finalEvent == null || finalEvent.result() == null) {
            return;
        }

        AiServerChatbotFinalResult result = finalEvent.result();
        String aiMessage = result.aiMessage();
        Boolean isCorrect = result.isCorrect();
        ParagraphType currentNode =
                StringUtils.hasText(result.currentNode())
                        ? ParagraphType.valueOf(result.currentNode())
                        : null;

        ChatbotConversation conversation =
                chatbotConversationRepository.findByIdWithAttempt(conversationId).orElse(null);
        if (conversation == null) {
            return;
        }

        if (aiMessage != null) {
            conversation.recordAiResponse(aiMessage, isCorrect);
        }

        if (currentNode != null) {
            ChatbotAttempt attempt = conversation.getAttempt();
            attempt.advanceToNextParagraph();
            chatbotAttemptRepository.save(attempt);
        }

        chatbotConversationRepository.save(conversation);
    }

    private void deleteConversationSilently(Long conversationId) {
        chatbotConversationRepository.deleteById(conversationId);
    }
}
