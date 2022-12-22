package com.mk.gateway;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class PassportGlobalFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return exchange.getPrincipal()
        //.filter(principal -> principal instanceof AbstractAuthenticationToken)
        .cast(Authentication.class)
        .map(this::token)
        .map(token -> withBearerAuth(exchange, token))
        .defaultIfEmpty(exchange)
        .flatMap(chain::filter);
  }

  @SneakyThrows
  private String token(Authentication authentication) {

    SecureRandom random = new SecureRandom();
    byte[] sharedSecret = new byte[32];
    random.nextBytes(sharedSecret);

    JWSSigner signer = new MACSigner(sharedSecret);

    String scope = authentication.getAuthorities().stream()
        .map(grantedAuthority -> grantedAuthority.getAuthority()).collect(
            Collectors.joining(" "));

    JWTClaimsSet claims = new JWTClaimsSet.Builder()
        .subject(authentication.getName())
        .audience("ipc")
        .issuer("gateway")
        .issueTime(new Date())
        .expirationTime(new Date(System.currentTimeMillis() + 600000))
        .jwtID(UUID.randomUUID().toString())
        .claim("scope", scope)
        .build();

    /*
     * TODO  type in header
     *
     */
    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);

    signedJWT.sign(signer);

    String token = signedJWT.serialize();

    log.info("El token {}", token);

    return token;

  }

  private ServerWebExchange withBearerAuth(ServerWebExchange exchange, String accessToken) {
    exchange.getResponse().getHeaders().add("test-token", accessToken);

    return exchange.mutate().request(r -> r.headers(headers -> headers.setBearerAuth(accessToken)))
        .build();
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
