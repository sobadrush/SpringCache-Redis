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
    public CacheManager primaryCacheManager(@Qualifier("redisConnectionFactoryDb0") LettuceConnectionFactory factoryDb0) {
        return this.createCacheManager(factoryDb0);
    }

    // 員工快取，使用 DB 6
    @Bean("empCacheManager")
    public CacheManager empCacheManager(@Qualifier("redisConnectionFactoryDb6") LettuceConnectionFactory factoryDb6) {
        return this.createCacheManager(factoryDb6);
    }

    // 部門快取，使用 DB 8
    @Bean("deptCacheManager")
    public CacheManager deptCacheManager(@Qualifier("redisConnectionFactoryDb8") LettuceConnectionFactory factoryDb8) {
        return this.createCacheManager(factoryDb8);
    }

    /**
     * 創建 RedisCacheManager，使用 LettuceConnectionFactory 和預設的序列化配置
     */
    private CacheManager createCacheManager(LettuceConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> cacheName + ":") // 改成單冒號, 避免 Another Redis Desktop Manager 解析預設的 :: 會出現 [empty]
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