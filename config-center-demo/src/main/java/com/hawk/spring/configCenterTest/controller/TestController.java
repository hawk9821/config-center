package com.hawk.spring.configCenterTest.controller;

import com.hawk.configCenter.annotation.RefreshScope;
import com.hawk.configCenter.service.ConfigCenterService;
import com.hawk.spring.configCenterTest.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author hawk9821
 * @Date 2020-03-16
 */
@RestController
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
@Slf4j
public class TestController {

    @Value("${xxx.name}")
    private String name;

    @Autowired
    private User user;

    @Autowired
    private ConfigCenterService centerService;

    @GetMapping("getUser")
    public User test(){
        log.info("============= getUser"  + user.toString());
        User result = new User();
        BeanUtils.copyProperties(user,result);
        log.info("User name :" + result.getName());
        log.info("User age :" + result.getAge());
        log.info("User sex :" + result.getSex());
        return result;
    }

    @PostMapping("updateUser")
    public boolean update(@RequestBody Map<String,String> data){
        centerService.addPropertyToRedis(data);
        return true;
    }


}
