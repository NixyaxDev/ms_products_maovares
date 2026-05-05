package com.maovares.ms_products.order.application.port.out;

public interface OrderQueueSender {
    void sendMessage(String message);
}
