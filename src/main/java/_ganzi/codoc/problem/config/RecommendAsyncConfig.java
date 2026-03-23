package _ganzi.codoc.problem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class RecommendAsyncConfig {

    @Bean(name = "recommendOnDemandTaskExecutor")
    public ThreadPoolTaskExecutor recommendOnDemandTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("recommend-ondemand-");
        executor.initialize();
        return executor;
    }
}
