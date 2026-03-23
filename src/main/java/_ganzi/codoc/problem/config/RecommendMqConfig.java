package _ganzi.codoc.problem.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConditionalOnProperty(prefix = "app.recommend.mq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties({RecommendMqProperties.class, RecommendDlqProperties.class})
public class RecommendMqConfig {

    @Bean
    public DirectExchange recommendExchange(RecommendMqProperties properties) {
        return new DirectExchange(properties.exchange(), true, false);
    }

    @Bean
    public Queue recommendRequestQueue(RecommendMqProperties properties) {
        return QueueBuilder.durable(properties.requestQueue())
                .deadLetterExchange(properties.exchange())
                .deadLetterRoutingKey(properties.requestDlqRoutingKey())
                .build();
    }

    @Bean
    public Queue recommendRequestDlq(RecommendMqProperties properties) {
        return QueueBuilder.durable(properties.requestDlq()).build();
    }

    @Bean
    public Queue recommendResponseQueue(RecommendMqProperties properties) {
        return QueueBuilder.durable(properties.responseQueue())
                .deadLetterExchange(properties.exchange())
                .deadLetterRoutingKey(properties.responseDlqRoutingKey())
                .build();
    }

    @Bean
    public Queue recommendResponseDlq(RecommendMqProperties properties) {
        return QueueBuilder.durable(properties.responseDlq()).build();
    }

    @Bean
    public Binding recommendRequestBinding(
            @Qualifier("recommendRequestQueue") Queue recommendRequestQueue,
            DirectExchange recommendExchange,
            RecommendMqProperties properties) {
        return BindingBuilder.bind(recommendRequestQueue)
                .to(recommendExchange)
                .with(properties.requestRoutingKey());
    }

    @Bean
    public Binding recommendResponseBinding(
            @Qualifier("recommendResponseQueue") Queue recommendResponseQueue,
            DirectExchange recommendExchange,
            RecommendMqProperties properties) {
        return BindingBuilder.bind(recommendResponseQueue)
                .to(recommendExchange)
                .with(properties.responseRoutingKey());
    }

    @Bean
    public Binding recommendRequestDlqBinding(
            @Qualifier("recommendRequestDlq") Queue recommendRequestDlq,
            DirectExchange recommendExchange,
            RecommendMqProperties properties) {
        return BindingBuilder.bind(recommendRequestDlq)
                .to(recommendExchange)
                .with(properties.requestDlqRoutingKey());
    }

    @Bean
    public Binding recommendResponseDlqBinding(
            @Qualifier("recommendResponseDlq") Queue recommendResponseDlq,
            DirectExchange recommendExchange,
            RecommendMqProperties properties) {
        return BindingBuilder.bind(recommendResponseDlq)
                .to(recommendExchange)
                .with(properties.responseDlqRoutingKey());
    }

    @Bean
    public MessageConverter recommendMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate recommendRabbitTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier("recommendMessageConverter") MessageConverter recommendMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(recommendMessageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory recommendRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("recommendMessageConverter") MessageConverter recommendMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(recommendMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean(name = "recommendDlqTaskExecutor")
    public ThreadPoolTaskExecutor recommendDlqTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("recommend-dlq-");
        executor.initialize();
        return executor;
    }
}
