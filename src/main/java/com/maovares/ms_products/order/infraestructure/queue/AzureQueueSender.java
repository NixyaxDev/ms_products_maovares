package com.maovares.ms_products.order.infraestructure.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.maovares.ms_products.order.application.port.out.OrderQueueSender;

@Component
public class AzureQueueSender implements OrderQueueSender {

    private static final Logger logger = LoggerFactory.getLogger(AzureQueueSender.class);

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    @Value("${azure.storage.queue-name:ordersqueue}")
    private String queueName;

    private QueueClient queueClient;

    /**
     * Lazy initialization — only connects when first message is sent,
     * so the app doesn't crash on startup if the variable is missing.
     */
    private QueueClient getQueueClient() {
        if (queueClient == null) {
            if (connectionString == null || connectionString.isEmpty()) {
                throw new RuntimeException("AZURE_STORAGE_CONNECTION_STRING is not configured");
            }
            this.queueClient = new QueueClientBuilder()
                    .connectionString(connectionString)
                    .queueName(queueName)
                    .buildClient();

            queueClient.createIfNotExists();
            logger.info("Azure Queue client initialized for queue: {}", queueName);
        }
        return queueClient;
    }

    @Override
    public void sendMessage(String message) {
        getQueueClient().sendMessage(message);
        logger.info("Message sent to Azure Queue '{}'", queueName);
    }
}
