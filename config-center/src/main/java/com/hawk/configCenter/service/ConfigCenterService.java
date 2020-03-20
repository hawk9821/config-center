package com.hawk.configCenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hawk.configCenter.autoConfig.RedisConstant;
import com.hawk.configCenter.scope.RefreshScopeRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author hawk9821
 * @Date 2020-03-16
 */
@Slf4j
public class ConfigCenterService implements ApplicationContextAware {
    @Value("${config.center.redis.enable:false}")
    private boolean enable;

    @Resource(name = "configCenterRedisTemplate")
    private RedisTemplate redisTemplate;

    @Resource
    private Environment environment;

    private ConfigurableApplicationContext applicationContext;

    private ConcurrentHashMap map = new ConcurrentHashMap();

    private BeanDefinitionRegistry beanDefinitionRegistry;

    @Resource
    private ObjectMapper objectMapper;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @PostConstruct
    public void init(){
        log.info("===================================== init redisConfigCenter");
        if (!enable) return;

        RefreshScopeRegistry refreshScopeRegistry = applicationContext.getBean(RefreshScopeRegistry.class);
        beanDefinitionRegistry = refreshScopeRegistry.getBeanDefinitionRegistry();
        try {
            Map<String, String> configs = redisTemplate.opsForHash().entries(RedisConstant.redisConfigCenterKey);
            //将配置信息加入到Spring容器的属性对象中
            initRedisConfigToSpringProperty(configs);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 修改属性并发布消息
     * @param properties
     */
    public void addPropertyToRedis(Map<String,String> properties){

        //事物确保Redis Hash更新后发布消息
        SessionCallback sessionCallback = new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                try {
                    redisOperations.multi();
                    redisOperations.opsForHash().putAll(RedisConstant.redisConfigCenterKey,properties);
                    Set<String> fields = properties.keySet();
                    String msg = objectMapper.writeValueAsString(fields);
                    redisOperations.convertAndSend(RedisConstant.redisTopic,msg);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return redisOperations.exec();
            }
        };
        redisTemplate.execute(sessionCallback);
    }

    /**
     * 根据消息发送的键值更新Spring容器的属性对象中对应的key-value
     * @param fields
     */
    public void addPropertyToSpring(Set fields){
        //从redis Hash 中取出配置属性的值
        for (Object field : fields) {
            Object data = redisTemplate.opsForHash().get(RedisConstant.redisConfigCenterKey, field);
            if (data !=null && !StringUtils.isEmpty(data)){
                MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
                for (PropertySource<?> propertySource : propertySources) {
                    if (RedisConstant.redisPropertyName.equals(propertySource.getName())){
                        OriginTrackedMapPropertySource ps = (OriginTrackedMapPropertySource) propertySource;
                        ConcurrentHashMap chm = (ConcurrentHashMap) ps.getSource();
                        chm.put(field,data.toString());
                    }
                }
            }
        }
        //对refresh作用域的实例进行刷新
        refreshBean();
    }

    private void refreshBean() {
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanDefinitionRegistry.getBeanDefinition(beanDefinitionName);
            if(RedisConstant.scopeName.equals(beanDefinition.getScope())) {
                //从Scope中删除实例
                applicationContext.getBeanFactory().destroyScopedBean(beanDefinitionName);
                //再重新实例化
                applicationContext.getBean(beanDefinitionName);
            }
        }
    }

    private void initRedisConfigToSpringProperty(Map<String, String> configs) {
        if (!checkExistsSpringProperty()) {
            //如果Spring中不存在redisSource的配置属性对象则创建
            createRedisSpringProperty();
        }

        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        PropertySource<?> propertySource = propertySources.get(RedisConstant.redisPropertyName);
        ConcurrentHashMap chm = (ConcurrentHashMap) propertySource.getSource();

        Set<String> keys = configs.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            chm.put(key,configs.get(key));
        }
    }


    private void createRedisSpringProperty() {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        OriginTrackedMapPropertySource redisSource = new OriginTrackedMapPropertySource(RedisConstant.redisPropertyName, map);
        propertySources.addLast(redisSource);
    }


    private boolean checkExistsSpringProperty() {
        MutablePropertySources propertySources = applicationContext.getEnvironment().getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            if (RedisConstant.redisPropertyName.equals(propertySource.getName())) {
                return true;
            }
        }
        return false;
    }
}
