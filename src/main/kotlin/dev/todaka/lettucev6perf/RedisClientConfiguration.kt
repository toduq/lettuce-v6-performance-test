package dev.todaka.lettucev6perf

import io.lettuce.core.ClientOptions
import io.lettuce.core.RedisURI
import io.lettuce.core.TimeoutOptions
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import io.lettuce.core.cluster.RedisClusterClient
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class RedisClusterConfiguration(
    val redisNodesProperties: RedisNodesProperties
) {
    @Bean
    fun asyncRedis(
        statefulConnection: StatefulRedisClusterConnection<String, String>
    ): RedisAdvancedClusterAsyncCommands<String, String> {
        return statefulConnection.async()
    }

    @Bean
    fun redisClusterClient(): StatefulRedisClusterConnection<String, String> {
        val redisURIs = redisNodesProperties.nodes.split(",").map { RedisURI.create(it) }
        val redisClusterClient = RedisClusterClient.create(redisURIs)
        val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enablePeriodicRefresh(Duration.ofSeconds(30))
            .enableAllAdaptiveRefreshTriggers()
            .build()
        val clientOptions = ClusterClientOptions
            .builder()
            .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(10)))
            .cancelCommandsOnReconnectFailure(false)
            .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
            .validateClusterNodeMembership(false)
            .topologyRefreshOptions(topologyRefreshOptions)
            .pingBeforeActivateConnection(true)
            .build()
        redisClusterClient.setOptions(clientOptions)
        return redisClusterClient.connect()
    }
}

/**
 * specify servers by ./gradlew bootRun --args='--redis.nodes=redis://192.0.2.1,redis://192.0.2.2,redis://192.0.2.3'
 */
@ConstructorBinding
@ConfigurationProperties("redis")
data class RedisNodesProperties(
    val nodes: String
)
