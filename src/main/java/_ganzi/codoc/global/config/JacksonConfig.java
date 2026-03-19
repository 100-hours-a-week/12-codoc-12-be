package _ganzi.codoc.global.config;

import java.util.TimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {

    @Bean
    public JsonMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .defaultTimeZone(TimeZone.getTimeZone("Asia/Seoul"))
                .build();
    }
}
