package com.roger.springcacheredis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ClientOptions;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author RogerLo
 * @date 2025/3/20
 */
@EnableCaching
@Configuration
public class CachedConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, GenericJackson2JsonRedisSerializer serializer) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(8)) // 默認沒特別指定的 cache 都會走這個規則
            .computePrefixWith(cacheName -> "caching:" + cacheName)
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // 🚩針對不同 cacheName，設置不同的過期時間
        Map<String, RedisCacheConfiguration> initialCacheConfiguration = new HashMap<String, RedisCacheConfiguration>() {{

            put("MY_CACHED_A", RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(1))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))); // 1 分鐘

            put("MY_CACHED_B", RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofSeconds(20))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))); // 20 秒

            // ...
        }};

        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig) // 默認配置（強烈建議配置上）比如動態創建出來的都會走此默認配置
                .withInitialCacheConfigurations(initialCacheConfiguration) // 不同 cache 的個性化配置
                .build();
        return redisCacheManager;
    }

    /**
     * 自定義緩存 Key 生成策略
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getName()).append(".");
            sb.append(method.getName()).append(":");
            for (Object param : params) {
                sb.append(param.toString()).append("-");
            }
            return sb.toString();
        };
    }


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
