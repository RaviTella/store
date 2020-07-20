package com.ratella.store.model.cart;

import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class CartRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CartCosmosDB cosmosDB;

    @Autowired
    public CartRepository(CartCosmosDB cosmosDB) {
        this.cosmosDB = cosmosDB;
    }

    public Mono<Cart> getCartById(String id) {
        return cosmosDB
                .getContainer()
                .readItem(id, new PartitionKey(id), Cart.class)
                .map(CosmosItemResponse::getItem)
                //If there is not cart for the id, just return a new cart instance
                .onErrorReturn(NotFoundException.class, new Cart());
    }

    public Mono<Integer> createCart(Cart cart) {
        return cosmosDB
                .getContainer()
                .createItem(cart)
                .map(CosmosItemResponse::getStatusCode);
    }

    public Mono<Integer> upsertCart(Cart cart) {
        return cosmosDB
                .getContainer()
                .upsertItem(cart)
                .map(CosmosItemResponse::getStatusCode);
    }

    private BigDecimal getPrice(String itemId, Cart cart) {
        return cart
                .getItems()
                .stream()
                .filter(item -> item.bookId.equals(itemId))
                .findFirst()
                .get()
                .getPrice();
    }

    public Mono<Integer> deleteCart(String id, String partitionKey) {
        return cosmosDB
                .getContainer()
                .deleteItem(id, new PartitionKey(partitionKey))
                .map(CosmosItemResponse::getStatusCode);
    }


}
