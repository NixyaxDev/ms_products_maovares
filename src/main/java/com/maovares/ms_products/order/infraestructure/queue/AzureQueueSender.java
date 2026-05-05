package com.maovares.ms_products.order.infraestructure.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.maovares.ms_products.order.application.port.out.OrderQueueSender;

import jakarta.annotation.PostConstruct;

@Component
public class AzureQueueSender implements OrderQueueSender {

    private static final Logger logger = LoggerFactory.getLogger(AzureQueueSender.class);

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.queue-name:ordersqueue}")
    private String queueName;

    private QueueClient queueClient;

    @PostConstruct
    public void init() {
        this.queueClient = new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .buildClient();

        // Create the queue if it doesn't exist
        queueClient.createIfNotExists();
        logger.info("Azure Queue client initialized for queue: {}", queueName);
    }

    @Override
    public void sendMessage(String message) {
        queueClient.sendMessage(message);
        logger.info("Message sent to Azure Queue '{}'", queueName);
    }
}
