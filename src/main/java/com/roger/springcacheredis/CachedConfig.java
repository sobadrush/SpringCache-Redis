package com.roger.springcacheredis;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * @author RogerLo
 * @date 2025/3/20
 */
@EnableCaching
@Configuration
public class CachedConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, RedisSerializer<Object> valuesSerializer) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(3)) // 設置默認過期時間為 1 小時
                .computePrefixWith(cacheName -> cacheName + ":");
        return new MyRedisCacheManager(
                RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory),
                defaultCacheConfig,
                new StringRedisSerializer(),
                valuesSerializer
        );
    }


    /**
     * 自訂 KeyGenerator
     * 產生規則範例：ClassName:MethodName:Param1,Param2
     */
    @Bean("myCustomKeyGenerator")
    public KeyGenerator myCustomKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder sb = new StringBuilder();
                // 1. 類別名稱
                sb.append(target.getClass().getSimpleName());
                sb.append(":");
                // 2. 方法名稱
                sb.append(method.getName());

                if (params.length > 0) {
                    sb.append(":");
                    // 3. 參數 (使用 StringUtils 串接，避免參數為 null 報錯)
                    String paramsStr = StringUtils.arrayToDelimitedString(params, ",");
                    sb.append(paramsStr);
                }

                return sb.toString();
            }
        };
    }

}
