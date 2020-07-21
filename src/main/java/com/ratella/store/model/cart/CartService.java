package com.ratella.store.model.cart;

import reactor.core.publisher.Mono;

public interface CartService {
    public Mono<Void> removeItemFromCart(String cartId, String itemId);
    public Mono<Integer> getNumberOfItemsInTheCart(String id);
    public Mono<Void> deleteCart(String id, String partitionKey);
    public Mono<Void> addItemToCart(String cartId, CartItem item);
    public Mono<Cart> getCart(String id);


}
