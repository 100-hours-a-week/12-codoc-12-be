package _ganzi.codoc.ai.infra;

import _ganzi.codoc.ai.dto.AiServerApiResponse;
import _ganzi.codoc.ai.dto.AiServerChatbotSendRequest;
import _ganzi.codoc.ai.dto.AiServerSessionFinishRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ChatbotClient {

    private static final String API_PATH_STREAM_MESSAGE = "/api/v2/chatbot";
    private static final String API_PATH_CANCEL_MESSAGE = "/api/v2/chatbot/{runId}";
    private static final String API_PATH_FINISH_SESSION = "/api/v2/chatbot/sessions";

    private final WebClient webClient;

    public ChatbotClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public Flux<ServerSentEvent<String>> streamMessage(AiServerChatbotSendRequest request) {
        return webClient
                .post()
                .uri(API_PATH_STREAM_MESSAGE)
                .bodyValue(request)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {});
    }

    public Mono<AiServerApiResponse<Void>> finishSession(AiServerSessionFinishRequest request) {
        return webClient
                .post()
                .uri(API_PATH_FINISH_SESSION)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<AiServerApiResponse<Void>> cancelMessage(Long runId) {
        return webClient
                .delete()
                .uri(API_PATH_CANCEL_MESSAGE, runId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }
}
