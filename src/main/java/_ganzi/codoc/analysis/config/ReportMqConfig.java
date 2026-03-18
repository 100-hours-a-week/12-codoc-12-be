package _ganzi.codoc.analysis.config;

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
@ConditionalOnProperty(prefix = "app.report.mq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(ReportMqProperties.class)
public class ReportMqConfig {

    @Bean
    public DirectExchange reportExchange(ReportMqProperties properties) {
        return new DirectExchange(properties.exchange(), true, false);
    }

    @Bean
    public Queue reportRequestQueue(ReportMqProperties properties) {
        return QueueBuilder.durable(properties.requestQueue()).build();
    }

    @Bean
    public Queue reportResponseQueue(ReportMqProperties properties) {
        return QueueBuilder.durable(properties.responseQueue()).build();
    }

    @Bean
    public Binding reportRequestBinding(
            Queue reportRequestQueue, DirectExchange reportExchange, ReportMqProperties properties) {
        return BindingBuilder.bind(reportRequestQueue)
                .to(reportExchange)
                .with(properties.requestRoutingKey());
    }

    @Bean
    public Binding reportResponseBinding(
            Queue reportResponseQueue, DirectExchange reportExchange, ReportMqProperties properties) {
        return BindingBuilder.bind(reportResponseQueue)
                .to(reportExchange)
                .with(properties.responseRoutingKey());
    }

    @Bean
    public MessageConverter reportMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate reportRabbitTemplate(
            ConnectionFactory connectionFactory,
            @Qualifier("reportMessageConverter") MessageConverter reportMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(reportMessageConverter);
        rabbitTemplate.setMandatory(true);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory reportRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            @Qualifier("reportMessageConverter") MessageConverter reportMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(reportMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
