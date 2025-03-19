package com.roger.springcacheredis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisTemplateTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    @DisplayName("[Test-001] 測試 RedisTemplate")
    void test_001() {
        String str = (String) redisTemplate.opsForValue().get("name");
        System.out.println("str = " + str);
    }

}
