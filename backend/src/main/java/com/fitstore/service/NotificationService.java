package com.fitstore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${fitstore.rabbitmq.exchange}")
    private String exchange;

    @Value("${fitstore.rabbitmq.routing-key}")
    private String routingKey;

    // ── PRODUCER: OrderService llama esto al confirmar pedido ─
    // El checkout NO espera el email — patrón Producer-Consumer
    public void publicarPedidoConfirmado(Long pedidoId, String emailCliente, String nombreCliente, Double total) {
        Map<String, Object> mensaje = Map.of(
            "pedidoId",      pedidoId,
            "email",         emailCliente,
            "nombre",        nombreCliente,
            "total",         total,
            "timestamp",     System.currentTimeMillis()
        );

        rabbitTemplate.convertAndSend(exchange, routingKey, mensaje);
        log.info("[RabbitMQ] Mensaje publicado → pedido #{} para {}", pedidoId, emailCliente);
    }

    // ── CONSUMER: escucha la cola y envía el email ────────────
    // Si SendGrid falla, RabbitMQ reintenta automáticamente
    @RabbitListener(queues = "${fitstore.rabbitmq.queue}")
    public void procesarNotificacion(Map<String, Object> mensaje) {
        Long   pedidoId = Long.valueOf(mensaje.get("pedidoId").toString());
        String email    = mensaje.get("email").toString();
        String nombre   = mensaje.get("nombre").toString();
        Double total    = Double.valueOf(mensaje.get("total").toString());

        log.info("[RabbitMQ Consumer] Procesando notificación pedido #{}", pedidoId);

        // En producción: llamada a SendGrid API
        // sendGridClient.send(email, buildEmailConfirmacion(pedidoId, nombre, total));

        // Simulación para desarrollo:
        log.info("📧 EMAIL ENVIADO → {}", email);
        log.info("   Asunto: ¡Tu pedido #{} fue confirmado!", pedidoId);
        log.info("   Hola {}, tu pedido por ${} está en camino.", nombre, total);
    }
}
