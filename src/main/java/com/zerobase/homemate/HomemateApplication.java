package com.zerobase.homemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EntityScan("com.zerobase.homemate.entity")
public class HomemateApplication {

  public static void main(String[] args) {
    SpringApplication.run(HomemateApplication.class, args);
  }

}
