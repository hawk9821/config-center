package com.hawk.spring.configCenterTest.config;

import com.hawk.configCenter.annotation.RefreshScope;
import com.hawk.spring.configCenterTest.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @Author hawk9821
 * @Date 2020-03-16
 */
@Configuration
@Slf4j
public class Config {

    @Bean
    @RefreshScope
    @ConfigurationProperties(prefix = "xxx")
    @Primary
    User user(){
      log.info("====================  @Bean User");
      return new User();
    }
}
