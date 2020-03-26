package com.hawk.spring.configCenterTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hawk.configCenter.service.ConfigCenterService;
import com.hawk.spring.configCenterTest.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
class ConfigCenterTestApplicationTests {

    @Autowired
    ConfigCenterService centerService;

    @Autowired
    User user;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${xxx.name}")
    private String name;

    @Test
    void contextLoads() throws JsonProcessingException, InterruptedException {
        init();
        User u = new User();
        BeanUtils.copyProperties(user,u);
        log.info(objectMapper.writeValueAsString(u));
        Map map = new HashMap();
        map.put("xxx.name","Hawk");
        map.put("xxx.age","29");
        centerService.addPropertyToRedis(map);
        TimeUnit.SECONDS.sleep(1);
        User u1 = new User();
        BeanUtils.copyProperties(user,u1);
        log.info(objectMapper.writeValueAsString(u1));
    }

    private void init() throws InterruptedException {
        Map map = new HashMap();
        map.put("xxx.name","Lily");
        map.put("xxx.age","18");
        map.put("xxx.sex","0");
        centerService.addPropertyToRedis(map);
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void vaule(){
        log.info("===========  name:"  +name);
    }

}
