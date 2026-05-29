package com.fitstore.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
public class InfraConfig {

    // ── Redis ────────────────────────────────────────────────
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    // ── RabbitMQ ─────────────────────────────────────────────
    @Value("${fitstore.rabbitmq.queue}")
    private String queueName;

    @Value("${fitstore.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${fitstore.rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public Queue pedidosQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange fitStoreExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue pedidosQueue, DirectExchange fitStoreExchange) {
        return BindingBuilder.bind(pedidosQueue).to(fitStoreExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
