package _ganzi.codoc.auth.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import _ganzi.codoc.auth.jwt.JwtAuthenticationFilter;
import _ganzi.codoc.user.enums.UserStatus;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String swaggerUiPath;
    private final String apiDocsPath;
    private final Environment environment;
    private final String actuatorAllowCidrs;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${springdoc.swagger-ui.path}") String swaggerUiPath,
            @Value("${springdoc.api-docs.path}") String apiDocsPath,
            Environment environment,
            @Value("${ACTUATOR_ALLOW_CIDRS:127.0.0.1/32}") String actuatorAllowCidrs) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.swaggerUiPath = swaggerUiPath;
        this.apiDocsPath = apiDocsPath;
        this.environment = environment;
        this.actuatorAllowCidrs = actuatorAllowCidrs;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] swaggerMatchers =
                new String[] {swaggerUiPath, swaggerUiPath + "/**", "/api/swagger-ui/**"};
        String[] apiDocsMatchers = new String[] {apiDocsPath, apiDocsPath + "/**"};
        boolean restrictActuator =
                Arrays.stream(environment.getActiveProfiles())
                        .anyMatch(profile -> profile.equals("dev") || profile.equals("prod"));
        RequestMatcher actuatorMatcher = new ActuatorCidrRequestMatcher(actuatorAllowCidrs);

        return http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .exceptionHandling(
                        handling ->
                                handling.authenticationEntryPoint(
                                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        authorize -> {
                            authorize
                                    .requestMatchers("/api/swagger-ui/index.html", "/swagger-ui.html", "/v3/api-docs")
                                    .permitAll()
                                    .requestMatchers(swaggerMatchers)
                                    .permitAll()
                                    .requestMatchers(apiDocsMatchers)
                                    .permitAll()
                                    .requestMatchers("/api/auth/**")
                                    .permitAll()
                                    .requestMatchers("/api/admin/**")
                                    .permitAll()
                                    .requestMatchers("/api/dev/auth/**")
                                    .permitAll();

                            if (restrictActuator) {
                                authorize.requestMatchers(actuatorMatcher).permitAll();
                                authorize.requestMatchers("/api/actuator/**", "/actuator/**").denyAll();
                            } else {
                                authorize.requestMatchers("/api/actuator/**", "/actuator/**").permitAll();
                            }

                            authorize
                                    .requestMatchers(HttpMethod.DELETE, "/api/user")
                                    .hasAnyAuthority(
                                            UserStatus.ONBOARDING.asAuthority(),
                                            UserStatus.ACTIVE.asAuthority(),
                                            UserStatus.DORMANT.asAuthority())
                                    .requestMatchers("/api/user/init-survey")
                                    .hasAuthority(UserStatus.ONBOARDING.asAuthority())
                                    .requestMatchers("/ws-chat/**")
                                    .permitAll()
                                    .requestMatchers("/api/health", "/api/ai-health")
                                    .permitAll()
                                    .anyRequest()
                                    .hasAuthority(UserStatus.ACTIVE.asAuthority());
                        })
                .build();
    }
}
