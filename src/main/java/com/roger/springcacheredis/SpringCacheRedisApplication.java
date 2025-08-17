package com.roger.springcacheredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ RedisConfig.class, CacheConfig.class })
public class SpringCacheRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCacheRedisApplication.class, args);
    }

}
