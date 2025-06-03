package com.insurance.infrastructure.messaging.service;

import com.insurance.event.PolicyRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(String exchange, String routingKey, PolicyRequestEvent event) {
        log.info("Publishing event {} to exchange {} with routing key {}", 
                 event.getEventType(), exchange, routingKey);
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
} 