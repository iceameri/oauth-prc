package com.jw.resourceserver.config;


import com.jw.resourceserver.controller.opened.BaseOpenedController;
import com.jw.resourceserver.controller.secured.BaseSecuredController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.List;

//@Profile({"local", "dev", "qa"})
@Configuration
public class SwaggerConfig {

    public final static String[] SWAGGER_PERMIT_ENDPOINTS = {
            "/swagger-ui/**",        // UI
            "/swagger-ui.html",      // 이전 방식
            "/v3/api-docs/**",       // API 문서
            "/v3/api-docs",          // 기본 문서
            "/swagger-resources/**", // (필요 시 추가)
            "/webjars/**"            // JS/CSS 리소스
    };

    @Value("${springdoc.server-url}")
    private String serverUrl;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final static String BEARER_JWT = "bearer-jwt";
    private final static String BEARER_ = OAuth2AccessToken.TokenType.BEARER.getValue();

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My API")
                        .version("v1")
                        .description("Swagger 문서입니다.")
                        .contact(new Contact().name("JW").email("jw@example.com"))
                )
                .servers(List.of(new Server()
                        .url(serverUrl)
                        .description(String.format("{%s} 환경 서버",  activeProfile))))
                /*Authorize 버튼*/
                .components(new Components()
                        .addSecuritySchemes(BEARER_JWT,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme(BEARER_)
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public-api")
                .pathsToMatch(BaseOpenedController.OPENED_API_PREFIX + "/**")
                .build();
    }

    @Bean
    public GroupedOpenApi securedApi() {
        return GroupedOpenApi.builder()
                .group("secured-api")
                .pathsToMatch(BaseSecuredController.SECURED_API_PREFIX + "/**")
                .build();
    }
}