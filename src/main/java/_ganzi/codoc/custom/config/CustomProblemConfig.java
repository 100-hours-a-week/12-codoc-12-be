package _ganzi.codoc.custom.config;

import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@EnableConfigurationProperties(CustomProblemProperties.class)
@Configuration
public class CustomProblemConfig {

    @Bean(name = "customProblemTaskExecutor")
    public Executor customProblemTaskExecutor(CustomProblemProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.async().corePoolSize());
        executor.setMaxPoolSize(properties.async().maxPoolSize());
        executor.setQueueCapacity(properties.async().queueCapacity());
        executor.setThreadNamePrefix(properties.async().threadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }
}
