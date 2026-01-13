package com.jw.resourceserver.controller.opened;

import com.jw.resourceserver.controller.secured.SecuredUserController;
import com.jw.resourceserver.dto.OAuth2TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@RestController
public class OpenedUserController extends BaseOpenedController {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final SecuredUserController securedUserController;

    public OpenedUserController(final JdbcTemplate jdbcTemplate,
                                final RestTemplate restTemplate,
                                final SecuredUserController securedUserController) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
        this.securedUserController = securedUserController;
    }

    @GetMapping("/login")
    public void login(
            @RequestHeader final HttpHeaders reqHeaders,
            final HttpServletResponse response,
            @RequestParam(name = "redirect_uri", required = false) final String redirectUri
    ) throws IOException {
        // 리디렉션 URL 구성
        String clientId = "test-client";
        String url = redirectUri != null ? redirectUri : "http://localhost:9091/authorized";
        String responseType = "code";
        String scope = "openid profile email";

        String redirectUrl = UriComponentsBuilder.fromUriString("http://localhost:9090/oauth2/authorize")
                .queryParam("response_type", responseType)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", url)
                .queryParam("scope", scope)
                .build()
                .toUriString();

        log.info("redirect url: {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/authorized")
    public ResponseEntity<OAuth2TokenResponse> handleAuthorizationCode(@RequestParam final String code) {
        log.info("Authorization code: {}", code);
        // 1. Access Token 요청
        String tokenUrl = "http://localhost:9090/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", "http://localhost:9091/authorized");
        body.add("client_id", "test-client");
        body.add("client_secret", "P@$$w0rd1!");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<OAuth2TokenResponse> response = this.restTemplate.postForEntity(tokenUrl, request, OAuth2TokenResponse.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            String accessToken = Objects.requireNonNull(response.getBody()).access_token();

            log.info("accessToken: {}", accessToken);

            // 2. Access Token 으로 사용자 정보 요청
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.setBearerAuth(accessToken);

//        HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);
//        ResponseEntity<Map<String, Object>> userInfoResponse = restTemplate.exchange(
//                "http://localhost:9090/auth/userinfo",
//                HttpMethod.GET,
//                userInfoRequest,
//                new ParameterizedTypeReference<Map<String, Object>>() {}
//        );
//
//        Map<String, Object> userInfo = userInfoResponse.getBody();
//        String email = (String) userInfo.get("email");
//
//        // 3. DB 저장 (MERGE 문, MSSQL 예시)
//        String sql = """
//            MERGE INTO users AS A
//            USING (SELECT ? AS email) AS B
//            ON A.email = B.email
//            WHEN MATCHED THEN
//                UPDATE SET email = B.email
//            /*WHEN NOT MATCHED THEN
//                INSERT (email) VALUES (B.email)*/;
//            """;
//        jdbcTemplate.update(sql, email);

            // 4. 결과 반환
            return ResponseEntity.ok(response.getBody());
        }

        return ResponseEntity.status(response.getStatusCode()).body(null);
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "No token needed for this endpoint.";
    }
}
