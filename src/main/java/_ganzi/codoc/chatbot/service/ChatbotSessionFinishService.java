package _ganzi.codoc.chatbot.service;

import _ganzi.codoc.ai.config.AiServerProperties;
import _ganzi.codoc.ai.dto.AiServerApiResponse;
import _ganzi.codoc.ai.dto.AiServerSessionFinishRequest;
import _ganzi.codoc.ai.infra.ChatbotClient;
import _ganzi.codoc.submission.domain.ProblemSession;
import _ganzi.codoc.submission.repository.ProblemSessionRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ChatbotSessionFinishService {

    private final ProblemSessionRepository problemSessionRepository;
    private final ChatbotClient chatbotClient;
    private final AiServerProperties aiServerProperties;

    @Transactional
    public void notifyFinishedSessions() {
        List<ProblemSession> sessions =
                problemSessionRepository.findUnnotifiedFinishedSessions(Instant.now());

        for (ProblemSession session : sessions) {
            notifyAiServer(session);
        }
    }

    private void notifyAiServer(ProblemSession session) {
        AiServerSessionFinishRequest request =
                AiServerSessionFinishRequest.of(
                        session.getId().toString(), session.getUser().getId(), session.getProblem().getId());

        try {
            AiServerApiResponse<Void> response =
                    chatbotClient.finishSession(request).block(aiServerProperties.baseTimeout());
            if (response == null) {
                log.warn(
                        "AI 서버 세션 종료 응답 없음: sessionId={}, userId={}, problemId={}",
                        session.getId(),
                        session.getUser().getId(),
                        session.getProblem().getId());
                return;
            }
            if (response.isFailure()) {
                log.warn(
                        "AI 서버 세션 종료 실패 응답: sessionId={}, userId={}, problemId={}, code={}, message={}",
                        session.getId(),
                        session.getUser().getId(),
                        session.getProblem().getId(),
                        response.code(),
                        response.message());
                return;
            }
            session.markExpired();
            session.markAiSessionNotified();
            log.info("AI 서버 세션 종료 통지 완료: sessionId={}", session.getId());
        } catch (Exception e) {
            log.warn(
                    "AI 서버 세션 종료 통지 실패: sessionId={}, userId={}, problemId={}, exceptionType={}",
                    session.getId(),
                    session.getUser().getId(),
                    session.getProblem().getId(),
                    e.getClass().getSimpleName(),
                    e);
        }
    }
}
