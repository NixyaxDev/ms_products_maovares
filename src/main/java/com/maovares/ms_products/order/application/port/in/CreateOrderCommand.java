package com.maovares.ms_products.order.application.port.in;

public interface CreateOrderCommand {
    String execute(String orderJson);
}
