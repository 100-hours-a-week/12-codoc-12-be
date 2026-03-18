package _ganzi.codoc.custom.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.custom-problem.mq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(CustomProblemMqProperties.class)
public class CustomProblemMqConfig {

    @Bean
    public DirectExchange customProblemExchange(CustomProblemMqProperties properties) {
        return new DirectExchange(properties.exchange(), true, false);
    }

    @Bean
    public Queue customProblemRequestQueue(CustomProblemMqProperties properties) {
        return QueueBuilder.durable(properties.requestQueue()).build();
    }

    @Bean
    public Queue customProblemResponseQueue(CustomProblemMqProperties properties) {
        return QueueBuilder.durable(properties.responseQueue())
                .deadLetterExchange(properties.exchange())
                .deadLetterRoutingKey(properties.responseDlq())
                .build();
    }

    @Bean
    public Queue customProblemResponseDlq(CustomProblemMqProperties properties) {
        return QueueBuilder.durable(properties.responseDlq()).build();
    }

    @Bean
    public Binding customProblemRequestBinding(
            @Qualifier("customProblemRequestQueue") Queue queue, DirectExchange customProblemExchange) {
        return BindingBuilder.bind(queue).to(customProblemExchange).with(queue.getName());
    }

    @Bean
    public Binding customProblemResponseBinding(
            @Qualifier("customProblemResponseQueue") Queue queue, DirectExchange customProblemExchange) {
        return BindingBuilder.bind(queue).to(customProblemExchange).with(queue.getName());
    }

    @Bean
    public Binding customProblemResponseDlqBinding(
            @Qualifier("customProblemResponseDlq") Queue queue, DirectExchange customProblemExchange) {
        return BindingBuilder.bind(queue).to(customProblemExchange).with(queue.getName());
    }
}
