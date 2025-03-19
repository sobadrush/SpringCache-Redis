package com.roger.springcacheredis;

import io.lettuce.core.ReadFrom;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringCacheRedisApplicationTests {

    @Test
    @DisplayName("[Test-001]")
    void test_001() {
        // 這裡只需要配置一個節點的連接信息，
        // 不一定需要是主節點的信息，
        // 從節點也可以，可以自動發現主從節點
        RedisURI uri = RedisURI.builder()
            .withHost("127.0.0.1")
            .withPort(6379)
            .withPassword("nanshan.1234".toCharArray())
            .withDatabase(3)
            .build();

        RedisClient client = RedisClient.create(uri);
        StatefulRedisMasterReplicaConnection<String, String> connection
                = MasterReplica.connect(client, StringCodec.UTF8, uri);

        // 此處設定為 ReadFrom.REPLICA 會發生錯誤，因為程式會一直嘗試透過 docker 容器的 IP 訪問從節點
        // Unable to connect to 172.21.0.3/<unresolved>:6379
        // Unable to connect to 172.21.0.4/<unresolved>:6379
        // 但是這兩個 IP 是 docker 容器的 IP，無法訪問
        // connection.setReadFrom(ReadFrom.REPLICA);

        // 從節點讀取數據
        connection.setReadFrom(ReadFrom.REPLICA_PREFERRED);

        RedisCommands<String, String> commands = connection.sync();
        commands.set("name", "張飛");
        System.out.println(commands.get("name"));

        connection.close();
        client.shutdown();
    }

}
