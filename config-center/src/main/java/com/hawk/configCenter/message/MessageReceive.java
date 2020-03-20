package com.hawk.configCenter.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hawk.configCenter.service.ConfigCenterService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Set;

/**
 * @Author hawk9821
 * @Date 2020-03-16
 */

@Slf4j
public class MessageReceive {

    @Resource
    private ConfigCenterService centerService;

    @Resource
    private ObjectMapper objectMapper;

    public void receiveMessage(String message){
        log.info("----------收到消息了message："+message);
        Set fields = null;
        try {
            fields = objectMapper.readValue(message, Set.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        centerService.addPropertyToSpring(fields);
    }
}
