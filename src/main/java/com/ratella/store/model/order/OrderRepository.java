package com.ratella.store.model.order;

import com.azure.cosmos.models.CosmosItemResponse;
import com.ratella.store.model.cart.CartCosmosDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OrderRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final OrderCosmosDB cosmosDB;

    @Autowired
    public OrderRepository(OrderCosmosDB cosmosDB) {
        this.cosmosDB = cosmosDB;
    }

    public Mono<Integer> createOrder(Order order) {
        return cosmosDB
                .getContainer()
                .createItem(order)
                .map(CosmosItemResponse::getStatusCode);
    }
}
