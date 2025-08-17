package com.roger.springcacheredis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ClientOptions.DisconnectedBehavior;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class RedisConfig {

    @Value("#{systemEnvironment['REDIS_SENTINEL_MASTER'] ?: 'mymaster'}")
    private String masterName;

    @Value("#{systemEnvironment['REDIS_SENTINEL_NODES'] ?: 'localhost:26379'}")
    private String sentinelNodes;

    @Value("#{systemEnvironment['REDIS_SENTINEL_PASSWORD'] ?: ''}")
    private String sentinelPassword;

    @Value("#{systemEnvironment['REDIS_MASTER_PASSWORD'] ?: ''}")
    private String masterPassword;

    @Value("#{systemEnvironment['REDIS_TIMEOUT'] ?: 30L}")
    private long timeoutMs;

    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactoryDb0() {
        return this.createLettuceConnectionFactory(0);
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactoryDb6() {
        return this.createLettuceConnectionFactory(6);
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactoryDb8() {
        return this.createLettuceConnectionFactory(8);
    }


    /**
     * 1. 當你的程式去問 sentinel：「mymaster 的 master 是誰？」
     *    Sentinel 回答：「在 redis-master:6379」。
     *    但你的應用程式所在環境找不到 redis-master 這個 DNS/主機名。
     *
     * 2. 如果你是本地開發或 Docker 環境，這個 redis-master 可能只是容器網路內部的名稱，對宿主機或 Spring Boot 來說根本不存在。
     *
     * 解法：在 /etc/hosts 加入：
     *          127.0.0.1   redis-master
     */
    private LettuceConnectionFactory createLettuceConnectionFactory(int database) {
        log.info("=== 開始創建 Redis Sentinel 連接工廠 (DB: {}) ===", database);

        // 解析 Sentinel 節點
        List<String> nodeList = this.parseSentinelNodes(sentinelNodes);
        log.info("解析到的 Sentinel 節點: {}", nodeList);

        // 創建 RedisSentinelConfiguration - 關鍵：不使用鏈式調用
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();

        // 先添加 Sentinel 節點，再設定 Master
        for (String node : nodeList) {
            String[] parts = node.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("無效的 Sentinel 節點格式: " + node);
            }

            String host = parts[0].trim();
            int port;
            try {
                port = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("無效的端口號: " + parts[1], e);
            }

            sentinelConfig.sentinel(host, port);
            log.info("添加 Sentinel 節點: {}:{}", host, port);
        }

        // 設定 Master 名稱
        sentinelConfig.setMaster(masterName);
        log.info("設定 Master 名稱: {}", masterName);

        // 設定資料庫
        sentinelConfig.setDatabase(database);
        log.info("設定資料庫: {}", database);

        // 設定 Master 密碼
        if (StringUtils.hasText(masterPassword)) {
            sentinelConfig.setPassword(masterPassword);
            log.info("已設定 Master 密碼");
        }

        // 設定 Sentinel 密碼
        if (StringUtils.hasText(sentinelPassword)) {
            sentinelConfig.setSentinelPassword(sentinelPassword);
            log.info("已設定 Sentinel 密碼");
        }

        // 驗證 Sentinel 配置
        if (sentinelConfig.getSentinels().isEmpty()) {
            throw new IllegalStateException("沒有配置任何 Sentinel 節點！");
        }

        log.info("Sentinel 配置驗證通過，節點數量: {}", sentinelConfig.getSentinels().size());

        // 創建客戶端配置 - 讓 Spring Boot 使用預設的 ClientResources
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                // 設定命令超時時間 : 當一個命令在指定時間內未完成時，將會拋出一個 TimeoutException
                .commandTimeout(Duration.ofSeconds(timeoutMs))
                // 設定關閉超時時間 : 當關閉 Redis 連接時，等待指定時間讓其完成
                .shutdownTimeout(Duration.ofSeconds(5))
                .clientOptions(ClientOptions.builder()
                        // 設定當連接斷開時的行為 : REJECT_COMMANDS 表示會立即拒絕新的命令
                        .disconnectedBehavior(DisconnectedBehavior.REJECT_COMMANDS)
                        // 設定使用的 Redis 協議版本 : RESP2 是 Redis 6 之前的版本，相容性好
                        .protocolVersion(ProtocolVersion.RESP2)
                        .socketOptions(SocketOptions.builder()
                                // 設定 TCP 連接超時時間
                                .connectTimeout(Duration.ofSeconds(timeoutMs))
                                // 啟用 TCP KeepAlive : 保持長連接，防止因網路閒置而被中斷
                                .keepAlive(true)
                                // 啟用 TCP NoDelay : 禁用 Nagle 演算法，降低延遲
                                .tcpNoDelay(true)
                                .build())
                        // 設定命令超時選項 : 這裡的 fixedTimeout 與上面的 commandTimeout 作用類似，用於更細粒度的超時控制
                        .timeoutOptions(TimeoutOptions.builder()
                                .fixedTimeout(Duration.ofSeconds(timeoutMs))
                                .build())
                        .build())
                .build();

        log.info("Lettuce 客戶端配置創建完成");

        // 創建連接工廠 - 這裡是關鍵！
        LettuceConnectionFactory factory = new LettuceConnectionFactory(sentinelConfig, clientConfig);

        // 重要：設定不驗證連接，避免啟動時立即嘗試連接
        factory.setValidateConnection(false);

        // 設定共享本地連接
        factory.setShareNativeConnection(true);

        try {
            factory.afterPropertiesSet();
            log.info("連接工廠初始化成功 - Master: {}, DB: {}", masterName, database);
        } catch (Exception e) {
            log.error("連接工廠初始化失敗", e);
            throw new RuntimeException("無法初始化 Redis 連接工廠", e);
        }

        return factory;
    }

    /**
     * 解析 Sentinel 節點字串
     */
    private List<String> parseSentinelNodes(String nodes) {
        log.info("開始解析 Sentinel 節點字串: {}", nodes);

        if (!StringUtils.hasText(nodes)) {
            throw new IllegalArgumentException("Sentinel 節點配置不能為空");
        }

        // 支援多種分隔符
        String[] nodeArray = nodes.split("[,;\s]+");
        List<String> result = new ArrayList<>();

        for (String node : nodeArray) {
            String trimmed = node.trim();
            if (StringUtils.hasText(trimmed)) {
                // 驗證節點格式
                if (!trimmed.matches("^[^:]+:\\d+$")) {
                    throw new IllegalArgumentException("無效的節點格式: " + trimmed + "，應為 host:port");
                }
                result.add(trimmed);
            }
        }

        if (result.isEmpty()) {
            throw new IllegalArgumentException("沒有找到有效的 Sentinel 節點: " + nodes);
        }

        log.info("成功解析 {} 個 Sentinel 節點: {}", result.size(), result);
        return result;
    }
}