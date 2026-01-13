package com.jw.resourceserver.controller.secured;

import com.jw.resourceserver.controller.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(BaseSecuredController.SECURED_API_PREFIX)
public abstract class BaseSecuredController extends BaseController {

    public static final String SECURED_API_PREFIX = BaseController.API_PREFIX + "/secure";

    protected JwtAuthenticationToken getJwtAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth;
        }

        return null;
    }

    @GetMapping("/userinfo")
    protected ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal final Jwt jwt) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", jwt.getSubject());
        response.put("scope", jwt.getClaimAsString("scope"));
        response.put("claims", jwt.getClaims());
        response.put("audience", jwt.getAudience());
        response.put("issuer", jwt.getIssuer());
        response.put("issuedAt", jwt.getIssuedAt());
        response.put("expiresAt", jwt.getExpiresAt());
        response.put("tokenValue", jwt.getTokenValue());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/userinfo/detailed")
    protected ResponseEntity<Map<String, Object>> getUserInfoDetailed() {
        JwtAuthenticationToken jwtAuth = this.getJwtAuthentication();
        if (jwtAuth == null) {
            return ResponseEntity.badRequest().build();
        }

        String userId = jwtAuth.getName();
        Map<String, Object> tokenAttributes = jwtAuth.getTokenAttributes();
        Jwt token = jwtAuth.getToken();
        Collection<GrantedAuthority> authorities = jwtAuth.getAuthorities();
        Object details = jwtAuth.getDetails();

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("tokenAttributes", tokenAttributes);
        response.put("token", token);
        response.put("authorities", authorities);
        response.put("details", details);

        return ResponseEntity.ok(response);
    }
}
