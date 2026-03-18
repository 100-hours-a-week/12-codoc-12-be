package _ganzi.codoc.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
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
@ConditionalOnProperty(prefix = "app.notification.mq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(NotificationMqProperties.class)
public class NotificationMqConfig {

    @Bean
    public TopicExchange notificationExchange(NotificationMqProperties properties) {
        return new TopicExchange(properties.exchange(), true, false);
    }

    @Bean
    public Queue notificationInAppQueue(NotificationMqProperties properties) {
        return QueueBuilder.durable(properties.inAppQueue())
                .deadLetterExchange(properties.exchange())
                .deadLetterRoutingKey("notification.dlq.inapp")
                .build();
    }

    @Bean
    public Queue notificationInAppDlq(NotificationMqProperties properties) {
        return QueueBuilder.durable(properties.inAppDlq()).build();
    }

    @Bean
    public Queue notificationPushQueue(NotificationMqProperties properties) {
        return QueueBuilder.durable(properties.pushQueue())
                .deadLetterExchange(properties.exchange())
                .deadLetterRoutingKey("notification.dlq.push")
                .build();
    }

    @Bean
    public Queue notificationPushDlq(NotificationMqProperties properties) {
        return QueueBuilder.durable(properties.pushDlq()).build();
    }

    @Bean
    public Binding notificationInAppBinding(
            @Qualifier("notificationInAppQueue") Queue notificationInAppQueue,
            TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationInAppQueue)
                .to(notificationExchange)
                .with("notification.#");
    }

    @Bean
    public Binding notificationPushBinding(
            @Qualifier("notificationPushQueue") Queue notificationPushQueue,
            TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationPushQueue)
                .to(notificationExchange)
                .with("notification.#");
    }

    @Bean
    public Binding notificationInAppDlqBinding(
            @Qualifier("notificationInAppDlq") Queue notificationInAppDlq,
            TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationInAppDlq)
                .to(notificationExchange)
                .with("notification.dlq.inapp");
    }

    @Bean
    public Binding notificationPushDlqBinding(
            @Qualifier("notificationPushDlq") Queue notificationPushDlq,
            TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationPushDlq)
                .to(notificationExchange)
                .with("notification.dlq.push");
    }

    @Bean
    public MessageConverter notificationMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate notificationRabbitTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier("notificationMessageConverter") MessageConverter notificationMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(notificationMessageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory notificationRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("notificationMessageConverter") MessageConverter notificationMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(notificationMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
