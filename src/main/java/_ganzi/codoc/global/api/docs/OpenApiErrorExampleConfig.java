package _ganzi.codoc.global.api.docs;

import _ganzi.codoc.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class OpenApiErrorExampleConfig {

    private static final String JSON_MEDIA_TYPE = "application/json";
    private static final String ERROR_SCHEMA_REF = "#/components/schemas/ApiResponse";

    @Bean
    public OperationCustomizer errorExampleCustomizer() {
        return (operation, handlerMethod) -> {
            ErrorCodes errorCodes = resolveErrorCodes(handlerMethod);
            if (errorCodes == null) {
                return operation;
            }

            List<ErrorCode> codes = collectErrorCodes(errorCodes);
            if (codes.isEmpty()) {
                return operation;
            }

            Map<String, List<ErrorCode>> byStatus = new LinkedHashMap<>();
            for (ErrorCode code : codes) {
                String status = Integer.toString(code.status().value());
                byStatus.computeIfAbsent(status, key -> new ArrayList<>()).add(code);
            }

            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }

            for (Map.Entry<String, List<ErrorCode>> entry : byStatus.entrySet()) {
                String statusCode = entry.getKey();
                io.swagger.v3.oas.models.responses.ApiResponse response =
                        operation
                                .getResponses()
                                .computeIfAbsent(
                                        statusCode, key -> new io.swagger.v3.oas.models.responses.ApiResponse());

                Content content = response.getContent();
                if (content == null) {
                    content = new Content();
                    response.setContent(content);
                }

                if (content.isEmpty()) {
                    content.addMediaType(JSON_MEDIA_TYPE, new MediaType());
                    content.addMediaType("*/*", new MediaType());
                }

                content.forEach(
                        (mediaTypeKey, mediaType) -> {
                            if (mediaType.getSchema() == null) {
                                mediaType.setSchema(new Schema<>().$ref(ERROR_SCHEMA_REF));
                            }

                            Map<String, Example> examples =
                                    mediaType.getExamples() == null
                                            ? new LinkedHashMap<>()
                                            : new LinkedHashMap<>(mediaType.getExamples());

                            for (ErrorCode code : entry.getValue()) {
                                if (examples.containsKey(code.code())) {
                                    continue;
                                }
                                Example example = new Example();
                                example.setSummary(code.code());
                                example.setDescription(code.message());
                                ObjectNode exampleValue = JsonNodeFactory.instance.objectNode();
                                exampleValue.put("code", code.code());
                                exampleValue.put("message", code.message());
                                exampleValue.putNull("data");
                                example.setValue(exampleValue);
                                examples.put(code.code(), example);
                            }

                            mediaType.setExamples(examples);
                        });
            }

            return operation;
        };
    }

    @Bean
    public OpenApiCustomizer errorResponseSchemaCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            if (components.getSchemas() != null
                    && components
                            .getSchemas()
                            .containsKey(_ganzi.codoc.global.dto.ApiResponse.class.getSimpleName())) {
                return;
            }

            Map<String, Schema> schemas =
                    ModelConverters.getInstance().read(_ganzi.codoc.global.dto.ApiResponse.class);
            Schema schema = schemas.get(_ganzi.codoc.global.dto.ApiResponse.class.getSimpleName());
            if (schema != null) {
                components.addSchemas(_ganzi.codoc.global.dto.ApiResponse.class.getSimpleName(), schema);
            }
        };
    }

    private static ErrorCodes resolveErrorCodes(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        ErrorCodes errorCodes = AnnotatedElementUtils.findMergedAnnotation(method, ErrorCodes.class);
        if (errorCodes != null) {
            return errorCodes;
        }

        Class<?> beanType = handlerMethod.getBeanType();
        for (Class<?> iface : beanType.getInterfaces()) {
            try {
                Method interfaceMethod = iface.getMethod(method.getName(), method.getParameterTypes());
                errorCodes = AnnotatedElementUtils.findMergedAnnotation(interfaceMethod, ErrorCodes.class);
                if (errorCodes != null) {
                    return errorCodes;
                }
            } catch (NoSuchMethodException ignore) {
            }
        }

        return null;
    }

    private static List<ErrorCode> collectErrorCodes(ErrorCodes errorCodes) {
        Set<ErrorCode> codes = new LinkedHashSet<>();
        Collections.addAll(codes, errorCodes.global());
        Collections.addAll(codes, errorCodes.submission());
        Collections.addAll(codes, errorCodes.problem());
        Collections.addAll(codes, errorCodes.user());
        Collections.addAll(codes, errorCodes.quest());
        Collections.addAll(codes, errorCodes.chatbot());
        Collections.addAll(codes, errorCodes.leaderboard());
        return new ArrayList<>(codes);
    }
}
