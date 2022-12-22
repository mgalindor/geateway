package com.mk.gateway;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

  public static final String CORRELATION_ID_HEADER = "correlation-id";

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String correlationId = UUID.randomUUID().toString().replace("-", "");

    exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

    ServerHttpRequest request = exchange.getRequest().mutate()
        .headers(httpHeaders -> httpHeaders.add(CORRELATION_ID_HEADER, correlationId)).build();

    return chain.filter(exchange.mutate().request(request).build());

  }

  @Override
  public int getOrder() {
    return 0;
  }
}
