package com.mk.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  /*@Bean
  public GlobalFilter correlationIdFilter(Tracer tracer) {
    return (exchange, chain) ->
    {
      Span span = tracer.currentSpan();
      log.info("{}",span);
      String traceId = span.context().traceId();
      exchange.getResponse().getHeaders().add("CORRELATION-ID",  traceId);
      return chain.filter(exchange);
    };
  }*/

  public static class CorrelationIdGlobalFilter {

  }
}
