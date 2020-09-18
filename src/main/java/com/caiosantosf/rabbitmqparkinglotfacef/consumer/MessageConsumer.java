package com.caiosantosf.rabbitmqparkinglotfacef.consumer;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.caiosantosf.rabbitmqparkinglotfacef.configuration.DirectExchangeConfiguration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class MessageConsumer {

    private static final String X_RETRIES_HEADER = "x-retries";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = DirectExchangeConfiguration.ORDER_MESSAGES_QUEUE_NAME)
    public void processOrderMessage(Message message) {
        log.info("Processing message: {}", message.toString());

        Integer retriesHeader = (Integer) message.getMessageProperties().getHeaders().get(X_RETRIES_HEADER);

        if (retriesHeader == null) {
            retriesHeader = Integer.valueOf(0);
        }

        if (retriesHeader >= 3) {
            log.info("Retries exeeded putting into parking lot");
            this.rabbitTemplate.send(DirectExchangeConfiguration.ORDER_MESSAGES_QUEUE_PARKINGLOT, message);
            return;
        }
        
        throw new RuntimeException("Business Rule Exception");
    }
}