package _ganzi.codoc.auth.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import _ganzi.codoc.auth.jwt.JwtAuthenticationFilter;
import _ganzi.codoc.user.enums.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final String swaggerUiPath;
    private final String apiDocsPath;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${springdoc.swagger-ui.path}") String swaggerUiPath,
            @Value("${springdoc.api-docs.path}") String apiDocsPath) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.swaggerUiPath = swaggerUiPath;
        this.apiDocsPath = apiDocsPath;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] swaggerMatchers =
                new String[] {swaggerUiPath, swaggerUiPath + "/**", "/api/swagger-ui/**"};
        String[] apiDocsMatchers = new String[] {apiDocsPath, apiDocsPath + "/**"};

        return http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers(
                                                "/api/swagger-ui/index.html", "/swagger-ui.html", "/v3/api-docs")
                                        .permitAll()
                                        .requestMatchers(swaggerMatchers)
                                        .permitAll()
                                        .requestMatchers(apiDocsMatchers)
                                        .permitAll()
                                        .requestMatchers("/api/auth/**")
                                        .permitAll()
                                        .requestMatchers(HttpMethod.DELETE, "/api/user")
                                        .hasAnyAuthority(
                                                UserStatus.ONBOARDING.asAuthority(),
                                                UserStatus.ACTIVE.asAuthority(),
                                                UserStatus.DORMANT.asAuthority())
                                        .requestMatchers("/api/user/init-survey")
                                        .hasAuthority(UserStatus.ONBOARDING.asAuthority())
                                        .requestMatchers("/api/health", "/api/ai-health")
                                        .permitAll()
                                        .anyRequest()
                                        .hasAuthority(UserStatus.ACTIVE.asAuthority()))
                .build();
    }
}
