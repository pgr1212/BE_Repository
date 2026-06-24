package com.example.kwu_graduation.global.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] SWAGGER_URLS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    // KLAS 쿠키 기반으로 자체 인증 처리하는 API들 - Spring Security의 authenticated()가 아니라
    // 컨트롤러/서비스에서 Klas-Cookie 헤더로 직접 검증하므로 여기서는 permitAll로 둔다.
    private static final String[] KLAS_API_URLS = {
            "/api/klas/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SWAGGER_URLS).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(KLAS_API_URLS).permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}