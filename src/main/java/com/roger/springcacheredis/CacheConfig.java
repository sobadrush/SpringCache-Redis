package com.roger.springcacheredis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("#{systemEnvironment['CACHE_TTL'] ?: 60L}") // 默認 60 秒
    private long cacheTtl;

    // 預設會使用主要的 CacheManager，使用 DB 0
    @Bean
    @Primary
    public CacheManager primaryCacheManager(@Qualifier("redisConnectionFactoryDb0") LettuceConnectionFactory redisConnectionFactoryDb0) {
        return this.createCacheManager(redisConnectionFactoryDb0);
    }

    // 員工快取，使用 DB 6
    @Bean("empCacheManager")
    public CacheManager empCacheManager(@Qualifier("redisConnectionFactoryDb6") LettuceConnectionFactory redisConnectionFactoryDb6) {
        return this.createCacheManager(redisConnectionFactoryDb6);
    }

    // 產品快取，使用 DB 2
    @Bean("productsCacheManager")
    public CacheManager productsCacheManager(@Qualifier("redisConnectionFactoryDb2") LettuceConnectionFactory redisConnectionFactoryDb2) {
        return this.createCacheManager(redisConnectionFactoryDb2);
    }

    /**
     * 創建 RedisCacheManager，使用 LettuceConnectionFactory 和預設的序列化配置
     */
    private CacheManager createCacheManager(LettuceConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(cacheTtl))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}