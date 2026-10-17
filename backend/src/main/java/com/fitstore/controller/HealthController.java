package com.fitstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health check de demostración: reporta el estado de cada dependencia
 * (SQL Server, Redis, RabbitMQ) sin romperse si alguna está caída.
 * Público — útil para mostrar en la exposición que el sistema está vivo.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("app",       "FitStore API");
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("database",  check(() -> jdbcTemplate.queryForObject("SELECT 1", Integer.class)));
        body.put("redis",     check(() -> redisTemplate.execute((RedisCallback<String>) conn -> {
                                  conn.ping();
                                  return "PONG";
                              })));
        body.put("rabbitmq",  check(() -> rabbitTemplate.getConnectionFactory()
                                  .createConnection().close()));

        boolean allUp = body.get("database").equals("UP")
                     && body.get("redis").equals("UP")
                     && body.get("rabbitmq").equals("UP");
        body.put("status", allUp ? "UP" : "DEGRADED");

        return ResponseEntity.ok(body);
    }

    private String check(Runnable probe) {
        try {
            probe.run();
            return "UP";
        } catch (Exception ex) {
            return "DOWN";
        }
    }
}
