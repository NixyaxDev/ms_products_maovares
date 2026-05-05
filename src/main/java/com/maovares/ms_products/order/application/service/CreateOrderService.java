package com.maovares.ms_products.order.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.maovares.ms_products.order.application.port.in.CreateOrderCommand;
import com.maovares.ms_products.order.application.port.out.OrderQueueSender;

@Service
public class CreateOrderService implements CreateOrderCommand {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderService.class);

    private final OrderQueueSender orderQueueSender;

    public CreateOrderService(OrderQueueSender orderQueueSender) {
        this.orderQueueSender = orderQueueSender;
    }

    @Override
    public String execute(String orderJson) {
        try {
            orderQueueSender.sendMessage(orderJson);
            logger.info("Order message sent to queue successfully");
            return "Order sent to queue successfully";
        } catch (Exception e) {
            logger.error("Failed to send order to queue", e);
            throw new RuntimeException("Failed to send order to queue: " + e.getMessage(), e);
        }
    }
}
