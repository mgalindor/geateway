package com.mk.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.OAuth2ResourceServerSpec;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

@Configuration
public class WebSecurityConfiguration {

  @Bean
  @Order(1)
  SecurityWebFilterChain jwtSecurityFilterChain(ServerHttpSecurity http,
      SecurityPathProperties securityPathProperties) {
    return http.csrf().disable()
        .securityMatcher(
            exchange -> hasAuthorizationHeaderType(exchange, "Bearer")
                ? MatchResult.match() : MatchResult.notMatch()
        )
        .authorizeExchange(authorize ->
            {
              securityPathProperties.getJwt().forEach(securityPath ->
                  authorize.pathMatchers(securityPath.path())
                      .hasAnyAuthority(securityPath.authorities())
              );
              authorize.anyExchange().denyAll();
            }
        )
        .oauth2ResourceServer(OAuth2ResourceServerSpec::jwt)
        .httpBasic().disable()
        .formLogin().disable()
        .build();
  }

  @Bean
  @Order(2)
  SecurityWebFilterChain basicSecurityFilterChain(ServerHttpSecurity http,
      SecurityPathProperties securityPathProperties) {
    return http.csrf().disable()
        .securityMatcher(exchange ->
            hasAuthorizationHeaderType(exchange, "Basic") ? MatchResult.match()
                : MatchResult.notMatch()
        )
        .authorizeExchange(authorize ->
            {
              securityPathProperties.getBasic().forEach(securityPath ->
                  authorize.pathMatchers(securityPath.path())
                      .hasAnyAuthority(securityPath.authorities())
              );
              authorize.anyExchange().denyAll();
            }
        )
        .httpBasic(Customizer.withDefaults())
        .formLogin().disable()
        .build();
  }

  @Bean
  @Order(3)
  SecurityWebFilterChain publicSecurityFilterChain(ServerHttpSecurity http,
      SecurityPathProperties securityPathProperties) {
    return http.csrf().disable()
        .securityMatcher(exchange ->
            hasAuthorizationHeader(exchange) ? MatchResult.notMatch() : MatchResult.match()
        )
        .authorizeExchange(authorize ->
            {
              securityPathProperties.getWhite().forEach(securityPath ->
                  authorize.pathMatchers(securityPath.path()).permitAll()
              );
              authorize.anyExchange().denyAll();
            }
        )
        .httpBasic().disable()
        .formLogin().disable()
        .build();
  }

  @Bean
  public MapReactiveUserDetailsService userDetailsService(
      SecurityPathProperties securityPathProperties) {
    UserDetails[] users = securityPathProperties.getUsers().stream().map(user ->
        User.builder()
            .username(user.name())
            .password(user.password())
            .authorities(user.authorities())
            .build()
    ).toList().toArray(UserDetails[]::new);

    return new MapReactiveUserDetailsService(users);
  }

  private boolean hasAuthorizationHeader(ServerWebExchange exchange) {
    return exchange.getRequest().getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).stream()
        .findFirst().isPresent();
  }

  private boolean hasAuthorizationHeaderType(ServerWebExchange exchange, String type) {
    return exchange.getRequest().getHeaders().getOrEmpty(HttpHeaders.AUTHORIZATION).stream()
        .anyMatch(s -> StringUtils.hasText(s) && StringUtils.startsWithIgnoreCase(s, type));
  }
}
