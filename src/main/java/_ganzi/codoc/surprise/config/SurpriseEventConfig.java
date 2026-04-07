package _ganzi.codoc.surprise.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SurpriseEventProperties.class)
public class SurpriseEventConfig {}
