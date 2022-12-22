package com.mk.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LogGlobalFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    log.info("Pre GatewayFilter logging");

    return chain.filter(exchange)
        // As alternative new TraceRunnable
        // Mono.fromRunnable(new TraceRunnable(tracing, spanNamer, () -> {
        .then(Mono.fromRunnable(() -> {
          // Post-processing
          log.info("Post GatewayFilter logging: ");
        }));
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
