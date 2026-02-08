package _ganzi.codoc.ai.infra;

import _ganzi.codoc.ai.dto.AiServerApiResponse;
import _ganzi.codoc.ai.dto.AiServerChatbotSendRequest;
import _ganzi.codoc.ai.dto.AiServerChatbotSendResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ChatbotClient {

    private static final String API_PATH_PREFIX_V1 = "/api/v1/chatbot";
    private static final String API_PATH_SEND_MESSAGE = API_PATH_PREFIX_V1;
    private static final String API_PATH_STREAM_MESSAGE =
            API_PATH_PREFIX_V1 + "/{conversationId}/stream";

    private final WebClient webClient;

    public ChatbotClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public Mono<AiServerApiResponse<AiServerChatbotSendResponse>> sendMessage(
            AiServerChatbotSendRequest request) {
        return webClient
                .post()
                .uri(API_PATH_SEND_MESSAGE)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Flux<ServerSentEvent<String>> streamMessage(Long conversationId) {
        WebClient.RequestHeadersSpec<?> uri =
                webClient.get().uri(API_PATH_STREAM_MESSAGE, conversationId);

        return uri.accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<>() {});
    }
}
