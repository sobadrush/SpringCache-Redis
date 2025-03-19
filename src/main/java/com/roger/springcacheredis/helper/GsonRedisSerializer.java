package com.roger.springcacheredis.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 自訂 Redis 序列化器
 *   - 改用 Gson 來序列化和反序列化
 *
 * @author RogerLo
 * @date 2025/3/19
 */
public class GsonRedisSerializer implements RedisSerializer<Object> {
    private final Gson gson = new Gson();
    private final Type type = new TypeToken<Object>() {}.getType(); // 使用通用 Object 類型

    @Override
    public byte[] serialize(Object object) throws SerializationException {
        if (object == null) {
            return new byte[0];
        }
        try {
            return gson.toJson(object).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SerializationException("Error serializing object to JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            String json = new String(bytes, StandardCharsets.UTF_8);
            return gson.fromJson(json, type); // 使用通用類型進行反序列化
        } catch (Exception e) {
            throw new SerializationException("Error deserializing JSON: " + e.getMessage(), e);
        }
    }
}
