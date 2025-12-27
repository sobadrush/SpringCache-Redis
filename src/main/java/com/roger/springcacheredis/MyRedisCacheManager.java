package com.roger.springcacheredis;

import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * @author RogerLo
 * @date 2025/12/27
 */
public class MyRedisCacheManager extends RedisCacheManager {

    // 1. 定義成員變數
    private final RedisSerializer<String> keysSerializer;
    private final RedisSerializer<Object> valuesSerializer;

    // 2. 修改建構子，傳入 GenericJackson2JsonRedisSerializer
    public MyRedisCacheManager(RedisCacheWriter cacheWriter,
                               RedisCacheConfiguration defaultCacheConfiguration,
                               RedisSerializer<String> keysSerializer,
                               RedisSerializer<Object> valuesSerializer) {
        super(cacheWriter, defaultCacheConfiguration);
        this.keysSerializer = keysSerializer;
        this.valuesSerializer = valuesSerializer;
    }

    @Override
    protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
        String[] array = StringUtils.delimitedListToStringArray(name, "#");
        name = array[0];
        if (array.length > 1 && StringUtils.hasText(array[1])) {
            try {
                long ttl = Long.parseLong(array[1]);
                cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(ttl)); // 設置自定義過期時間(可自訂單位)
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                    "Invalid TTL format in cache name '" + name + "': '" + array[1] + "' is not a valid number of seconds.", ex
                );
            }
        }

        // 3. 使用注入進來的 serializer，而不是 new 一個新的
        cacheConfig = cacheConfig.serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(this.keysSerializer)
        ).serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(this.valuesSerializer)
        );

        return super.createRedisCache(name, cacheConfig);
    }
}
