package _ganzi.codoc.auth.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import _ganzi.codoc.user.enums.UserStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers("/api/auth/**")
                                        .permitAll()
                                        .requestMatchers("/api/user/init-survey")
                                        .hasRole(UserStatus.ONBOARDING.toString())
                                        .requestMatchers("/api/health")
                                        .permitAll()
                                        .anyRequest()
                                        .hasRole(UserStatus.ACTIVE.toString()))
                .build();
    }
}
