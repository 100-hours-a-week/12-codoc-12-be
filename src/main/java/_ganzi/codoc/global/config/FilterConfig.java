package _ganzi.codoc.global.config;

import _ganzi.codoc.global.log.LoggingContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public LoggingContextFilter loggingContextFilter() {
        return new LoggingContextFilter();
    }

    @Bean
    public FilterRegistrationBean<LoggingContextFilter> loggingContextFilterRegistration(
            LoggingContextFilter filter) {
        FilterRegistrationBean<LoggingContextFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(Integer.MIN_VALUE);
        return bean;
    }
}
