# Redis实现配置中心

### 工程介绍：

* config-center ： 源码工程
* config-center-demo ： 使用样例工程

配置中心采用Redis Hash作为配置的持久化存储，publish / subscribe 作为消息通知，通知客户端配置变更，动态刷新配置。

### 使用步骤及样例介绍：

1.  打包安装config-center工程
   
   ```bash
   mvn clean package install 
   ```
   
2. 样例工程引入jar包

   ```xml
   <dependency>
       <groupId>com.hawk.spring.spring-cloud</groupId>
       <artifactId>config-center</artifactId>
       <version>0.0.1</version>
   </dependency>
   ```

3. 配置文件配置开启配置中心功能 `config.center.redis.enable=true` ， 该功能强依赖 RedisTemplate，同时需要配置RedisTemplate相关配置

   ```properties
   #开启配置中心
   config.center.redis.enable=true
   #RedisTemplate
   spring.redis.host=192.168.220.98
   spring.redis.port=6379
   spring.redis.password=
   spring.redis.jedis.pool.max-idle=8
   spring.redis.jedis.pool.min-idle=0
   spring.redis.jedis.pool.max-wait=1000
   ```

4. 动态刷新功能使用，在需要动态刷新的类上添加 `@Resfresh` 注解 

   ```java
   @Bean
   @RefreshScope
   @ConfigurationProperties(prefix = "xxx")
   @Primary
   User user(){
       log.info("====================  @Bean User");
       return new User();
   }
   ```

   ```java
   @RestController
   @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
   @Slf4j
   public class TestController {
   
       @Value("${xxx.name}")
       private String name;
   ...
   ```

   如此，遍可以实现 User 实体类 和 name属性的动态刷新。

5. 修改配置入口方法 , 注入 `ConfigCenterService` ，调用  `addPropertyToRedis` 方法

   ```java
   @Autowired
   ConfigCenterService centerService;
   ...
   Map map = new HashMap();
   map.put("xxx.name","Hawk");
   map.put("xxx.age","29");
   centerService.addPropertyToRedis(map);
   ...
   ```

   详见 demo 工程测试样例。



