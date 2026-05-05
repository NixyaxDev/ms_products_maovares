package com.maovares.ms_products.order.infraestructure.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maovares.ms_products.order.application.port.in.CreateOrderCommand;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Orders", description = "API for order management (event-driven)")
@RestController
@RequestMapping("/v1/orders")
public class OrderController {

    private final CreateOrderCommand createOrderCommand;

    public OrderController(CreateOrderCommand createOrderCommand) {
        this.createOrderCommand = createOrderCommand;
    }

    @Operation(summary = "Create an order", description = "Receives order JSON and sends it as a message to Azure Storage Queue 'ordersqueue'.")
    @ApiResponse(responseCode = "202", description = "Order accepted and sent to queue")
    @ApiResponse(responseCode = "500", description = "Failed to send order to queue")
    @PostMapping
    public ResponseEntity<Map<String, String>> createOrder(@RequestBody String orderJson) {
        try {
            String result = createOrderCommand.execute(orderJson);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(Map.of(
                            "status", "accepted",
                            "message", result
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", e.getMessage()
                    ));
        }
    }
}
