package _ganzi.codoc.chatbot.scheduler;

import _ganzi.codoc.chatbot.service.ChatbotSessionFinishService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatbotSessionFinishScheduler {

    private final ChatbotSessionFinishService chatbotSessionFinishService;

    @Scheduled(fixedRateString = "${app.schedule.chatbot-session-finish-fixed-rate:600000}")
    public void notifyFinishedSessions() {
        chatbotSessionFinishService.notifyFinishedSessions();
    }
}
