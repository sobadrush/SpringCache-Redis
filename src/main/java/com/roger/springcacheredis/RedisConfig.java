package com.roger.springcacheredis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * @author RogerLo
 * @date 2025/12/27
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 配置 Redis 服務器地址、端口、密碼
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName("127.0.0.1");
        redisConfig.setPort(6379);
        redisConfig.setPassword("nanshan.1234"); // 若無密碼，可省略
        redisConfig.setDatabase(3);

        // 配置 Lettuce 客戶端
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5)) // 設置超時時間
                .shutdownTimeout(Duration.ofMillis(100)) // 關閉連接超時時間
                .clientOptions(ClientOptions.builder().autoReconnect(true).build()) // 自動重連
                .build();

        // 創建 LettuceConnectionFactory
        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    // 以下寫法可以讓 @Cacheable 不會出錯：
    // 於 Redis 會產生如下數據，多塞一個「類型 class」，使之能順利被反序列化：
    //     [
    //         "com.roger.springcacheredis.entities.EmpVO",
    //         {
    //             "empNo": 7003,
    //             "empName": "Cathy",
    //             "empAge": 23
    //         }
    //     ]
    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping( // 啟用預設的類型
            objectMapper.getPolymorphicTypeValidator(), // 預設的類型驗證器
            ObjectMapper.DefaultTyping.NON_FINAL // 非最終類型
        );
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, GenericJackson2JsonRedisSerializer serializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer); // 預設使用 Jackson2Json 序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer); // 預設使用 Jackson2Json 序列化
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

}
