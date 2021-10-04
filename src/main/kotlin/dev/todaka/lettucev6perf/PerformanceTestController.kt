package dev.todaka.lettucev6perf

import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*
import java.util.concurrent.CompletableFuture


@RestController
class PerformanceTestController(
    private val redis: RedisAdvancedClusterAsyncCommands<String, String>
) {
    companion object {
        val value = mapOf("123" to "456", "abc" to "def", "ABC" to "DEFG")
    }

    @GetMapping("/hash/{count}")
    fun hash(@PathVariable count: Int): String {
        val uuid = UUID.randomUUID().toString()
        run {
            val cfs = (0 until count).map { redis.hset("$uuid-$it", value).toCompletableFuture() }
            CompletableFuture.allOf(*cfs.toTypedArray()).get()
        }

        run {
            val cfs = (0 until count).map { redis.hgetall("$uuid-$it").toCompletableFuture() }
            cfs.forEach {
                val v = it.get()
                if (v != value) {
                    throw RuntimeException("Invalid value found $v")
                }
            }
        }

        run {
            val cfs = (0 until count).map { redis.del("$uuid-$it").toCompletableFuture() }
            CompletableFuture.allOf(*cfs.toTypedArray()).get()
        }
        return "OK"
    }
}
