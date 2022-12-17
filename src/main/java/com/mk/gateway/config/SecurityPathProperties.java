package com.mk.gateway.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties("spring.security.paths")
public class SecurityPathProperties {

  private List<SecurityPath> basic = new ArrayList<>();
  private List<SecurityPath> jwt = new ArrayList<>();
  private List<SecurityPath> white = new ArrayList<>();
  private List<UserDetail> users = new ArrayList<>();

  @ConstructorBinding
  record SecurityPath(String path, String[] authorities) {

  }

  @ConstructorBinding
  record UserDetail(String name, String password, String[] authorities) {

  }
}

