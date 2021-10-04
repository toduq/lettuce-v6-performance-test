package dev.todaka.lettucev6perf

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(RedisNodesProperties::class)
@SpringBootApplication
class LettuceV6PerformanceTestApplication

fun main(args: Array<String>) {
    runApplication<LettuceV6PerformanceTestApplication>(*args)
}
