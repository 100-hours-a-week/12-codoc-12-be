package _ganzi.codoc.ai.infra;

import _ganzi.codoc.ai.dto.AiServerChatbotCancelResponse;
import _ganzi.codoc.ai.dto.AiServerChatbotSendRequest;
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
    private static final String API_PATH_CANCEL_STREAM = "/api/v2/chatbot/{runId}";

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

    public Mono<AiServerChatbotCancelResponse> cancelStream(Long runId) {
        return webClient
                .delete()
                .uri(API_PATH_CANCEL_STREAM, runId)
                .retrieve()
                .bodyToMono(AiServerChatbotCancelResponse.class);
    }
}
