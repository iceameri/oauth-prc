package com.jw.authorizationserver;

/*디버그 중 필요한 데이터 저장용*/
public class AuthorizationDebugTest {
    /* OAuth2Authorization 객체
{
  "@class" : "org.springframework.security.oauth2.server.authorization.OAuth2Authorization",
  "id" : "4e73d762-2c81-4f4f-9b53-9d31710b9b65",
  "registeredClientId" : "1",
  "principalName" : "qwer",
  "authorizationGrantType" : {
    "value" : "authorization_code"
  },
  "authorizedScopes" : [ "java.util.Collections$UnmodifiableSet", [ "openid", "profile", "email" ] ],
  "attributes" : {
    "@class" : "java.util.Collections$UnmodifiableMap",
    "java.security.Principal" : {
      "@class" : "org.springframework.security.authentication.UsernamePasswordAuthenticationToken",
      "authorities" : [ "java.util.Collections$UnmodifiableRandomAccessList", [ {
        "@class" : "org.springframework.security.core.authority.SimpleGrantedAuthority",
        "authority" : "ROLE_USER"
      } ] ],
      "details" : {
        "@class" : "org.springframework.security.web.authentication.WebAuthenticationDetails",
        "remoteAddress" : "0:0:0:0:0:0:0:1",
        "sessionId" : "A6836256D6281EDF15B84B6F63B93AB5"
      },
      "authenticated" : true,
      "principal" : {
        "@class" : "org.springframework.security.core.userdetails.User",
        "password" : null,
        "username" : "qwer",
        "authorities" : [ "java.util.Collections$UnmodifiableSet", [ {
          "@class" : "org.springframework.security.core.authority.SimpleGrantedAuthority",
          "authority" : "ROLE_USER"
        } ] ],
        "accountNonExpired" : true,
        "accountNonLocked" : true,
        "credentialsNonExpired" : true,
        "enabled" : true
      },
      "credentials" : null
    },
    "org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest" : {
      "@class" : "org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest",
      "authorizationUri" : "http://localhost:9090/oauth2/authorize",
      "authorizationGrantType" : {
        "value" : "authorization_code"
      },
      "responseType" : {
        "value" : "code"
      },
      "clientId" : "test-client",
      "redirectUri" : "http://localhost:9090/oauth2/token",
      "scopes" : [ "java.util.Collections$UnmodifiableSet", [ "openid", "profile", "email" ] ],
      "state" : null,
      "additionalParameters" : {
        "@class" : "java.util.Collections$UnmodifiableMap",
        "continue" : ""
      },
      "authorizationRequestUri" : "http://localhost:9090/oauth2/authorize?response_type=code&client_id=test-client&scope=openid%20profile%20email&redirect_uri=http://localhost:9090/oauth2/token&continue=",
      "attributes" : {
        "@class" : "java.util.Collections$UnmodifiableMap"
      }
    }
  },
  "refreshToken" : null,
  "accessToken" : null
}
*/
}
