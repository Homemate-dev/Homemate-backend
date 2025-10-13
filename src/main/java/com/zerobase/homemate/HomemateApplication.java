package com.zerobase.homemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HomemateApplication {

  public static void main(String[] args) {
    SpringApplication.run(HomemateApplication.class, args);
  }

}
