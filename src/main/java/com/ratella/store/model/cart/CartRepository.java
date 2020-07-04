package com.ratella.store.model.cart;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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

    public Mono<Integer> getCartItemCount1(String id) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setMaxBufferedItemCount(10);
        String query = "SELECT * FROM cart c WHERE c.id = " + "'" + id + "'";
        return cosmosDB
                .getContainer()
                .queryItems(
                        query, queryOptions, Cart.class)
                .byPage()
                .flatMap(feedResponse -> Flux.fromIterable(feedResponse.getElements()))
                .map(cart -> {
                    int itemCount = cart == null || cart.getItems() == null ? 0 : cart
                            .getItems()
                            .size();
                    return itemCount;
                })
                .single();

    }

    public Mono<Integer> getCartItemCount(String id) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setMaxBufferedItemCount(10);
        String query = "SELECT * FROM cart c WHERE c.id = " + "'" + id + "'";
        return cosmosDB
                .getContainer()
                .queryItems(
                        query, queryOptions, Cart.class)
                .byPage()
                .map(feedResponse -> {
                    int itemCount = feedResponse
                            .getResults()
                            //if there is no cart then count is zero, otherwise get the cart item count
                            .size() == 0 ? 0 : feedResponse
                            .getElements()
                            .stream()
                            .findFirst()
                            .get()
                            .getItems()
                            .size();
                    return itemCount;
                })
                .single();

    }


    public Mono<Cart> getCart(String id) {
        return cosmosDB
                .getContainer()
                .readItem(id, new PartitionKey(id), Cart.class)
                .map(CosmosItemResponse::getItem);
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


    public Mono<Integer> addCartItem(String cartId, CartItem item) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setMaxBufferedItemCount(10);
        String query = "SELECT * FROM cart c WHERE c.id = " + "'" + cartId + "'";
        return cosmosDB
                .getContainer()
                .queryItems(
                        query, queryOptions, Cart.class)
                .byPage()
                .flatMap(feedResponse -> {
                    if (feedResponse
                            .getResults()
                            .size() == 0) { //There is no cart
                        logger.info("THERE IS NO CART");
                        Cart cart = new Cart();
                        cart.setId(cartId);
                        cart.setSubTotal(item.price);
                        cart
                                .getItems()
                                .add(item);
                        return upsertCart(cart);
                    } else { //There is a cart
                        Cart cart = feedResponse
                                .getResults()
                                .get(0);
                        cart
                                .getItems()
                                .add(item);
                        cart.setSubTotal(cart
                                .getItems()
                                .stream()
                                .map(CartItem::getPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                        return upsertCart(cart);
                    }

                })
                .single();
    }

}
