package _ganzi.codoc.ai.infra;

import _ganzi.codoc.ai.dto.AiServerChatbotSendRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class ChatbotClient {

    private static final String API_PATH_PREFIX = "/api/v2/chatbot";
    private static final String API_PATH_STREAM_MESSAGE = API_PATH_PREFIX;

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
}
