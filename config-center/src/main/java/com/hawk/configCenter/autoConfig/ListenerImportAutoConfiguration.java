package com.hawk.configCenter.autoConfig;

import com.hawk.configCenter.message.MessageReceive;
import com.hawk.configCenter.service.ConfigCenterService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @Author hawk9821
 * @Date 2020-03-16
 */
@Import({ConfigCenterService.class})
@ConditionalOnProperty(prefix = "config.center.redis" ,name = "enable",havingValue = "true",matchIfMissing = false)
public class ListenerImportAutoConfiguration {

    @Bean
    public MessageReceive receive(){
        return new MessageReceive();
    }

    /**
     * 消息监听器，使用MessageAdapter可实现自动化解码及方法代理
     *
     * @return
     */
    @Bean
    public MessageListenerAdapter listener(MessageReceive receive) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(receive, "receiveMessage");
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }

    /**
     * 将订阅器绑定到容器
     *
     * @param connectionFactory
     * @param listenerAdapter
     * @return
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(RedisConstant.redisTopic));
        return container;
    }

}
