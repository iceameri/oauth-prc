package com.jw.resourceserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final static String[] PERMIT_ENDPOINTS = {
            "/api/opened/**",
            "/login",
            "/authorized",
            "/error",
            "/public",
            "/favicon.ico"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        List<String> PERMIT_URI = new ArrayList<>(Arrays.asList(PERMIT_ENDPOINTS));

        if (!activeProfile.equals("prod")) {
            PERMIT_URI.addAll(Arrays.asList(SwaggerConfig.SWAGGER_PERMIT_ENDPOINTS));
        }

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PERMIT_URI.toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()

                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // 필요에 따라 prefix 설정
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); // 클레임 이름 (예: scope, roles 등)

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
