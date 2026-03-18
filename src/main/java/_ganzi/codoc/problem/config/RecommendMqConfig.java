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

@Configuration
@ConditionalOnProperty(prefix = "app.recommend.mq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RecommendMqProperties.class)
public class RecommendMqConfig {

    @Bean
    public DirectExchange recommendExchange(RecommendMqProperties properties) {
        return new DirectExchange(properties.exchange(), true, false);
    }

    @Bean
    public Queue recommendRequestQueue(RecommendMqProperties properties) {
        return QueueBuilder.durable(properties.requestQueue()).build();
    }

    @Bean
    public Queue recommendResponseQueue(RecommendMqProperties properties) {
        return QueueBuilder.durable(properties.responseQueue()).build();
    }

    @Bean
    public Binding recommendRequestBinding(
            Queue recommendRequestQueue,
            DirectExchange recommendExchange,
            RecommendMqProperties properties) {
        return BindingBuilder.bind(recommendRequestQueue)
                .to(recommendExchange)
                .with(properties.requestRoutingKey());
    }

    @Bean
    public Binding recommendResponseBinding(
            Queue recommendResponseQueue,
            DirectExchange recommendExchange,
            RecommendMqProperties properties) {
        return BindingBuilder.bind(recommendResponseQueue)
                .to(recommendExchange)
                .with(properties.responseRoutingKey());
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
}
