package _ganzi.codoc.ai.infra;

import _ganzi.codoc.ai.config.AiServerProperties;
import _ganzi.codoc.ai.dto.AiServerApiResponse;
import _ganzi.codoc.ai.dto.AiServerChatbotSendRequest;
import _ganzi.codoc.ai.dto.AiServerChatbotSendResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class ChatbotClient {

    private static final String API_PATH_PREFIX_V1 = "/api/v1/chatbot";
    private static final String API_PATH_SEND_MESSAGE = API_PATH_PREFIX_V1;

    private final WebClient webClient;
    private final AiServerProperties aiServerProperties;

    public ChatbotClient(WebClient.Builder builder, AiServerProperties aiServerProperties) {
        this.aiServerProperties = aiServerProperties;
        this.webClient = builder.build();
    }

    public AiServerApiResponse<AiServerChatbotSendResponse> sendMessage(
            AiServerChatbotSendRequest request) {
        return webClient
                .post()
                .uri(API_PATH_SEND_MESSAGE)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<AiServerApiResponse<AiServerChatbotSendResponse>>() {})
                .timeout(aiServerProperties.baseTimeout())
                .block();
    }
}
