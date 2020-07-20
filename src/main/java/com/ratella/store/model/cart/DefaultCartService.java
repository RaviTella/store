package com.ratella.store.model.cart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class DefaultCartService implements CartService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private CartRepository cartRepository;

    @Autowired
    DefaultCartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Mono<Integer> getNumberOfItemsInTheCart(String id) {
        return cartRepository
                .getCartById(id)
                .map(cart -> {
                    return cart
                            .getItems()
                            .size();
                });
    }

    @Override
    public Mono<Void> deleteCart(String id, String partitionKey) {
        return cartRepository
                .deleteCart(id, partitionKey)
                .then();
    }

    @Override
    public Mono<Cart> getCart(String id) {
        return cartRepository.getCartById(id);
    }

    public Mono<Void> addItemToCart(String cartId, CartItem item) {
        return cartRepository
                .getCartById(cartId)
                .flatMap(cart -> {
                    if (cart.getId() == null) {
                        cart.setId(cartId);
                        cart.setSubTotal(item.price);
                        cart
                                .getItems()
                                .add(item);
                        return cartRepository.saveCart(cart);
                    } else {
                        cart
                                .getItems()
                                .add(item);
                        cart.setSubTotal(cart
                                .getItems()
                                .stream()
                                .map(CartItem::getPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                        return cartRepository.upsertCart(cart);
                    }
                })
                .then();

    }

    @Override
    public Mono<Void> removeItemFromCart(String cartId, String itemId) {
        return cartRepository
                .getCartById(cartId)
                .flatMap(cart -> {
                    if (cart
                            .getItems()
                            .size() == 1) return cartRepository.deleteCart(cartId, cartId);
                    logger.info("Getting price of the item to be deleted");
                    BigDecimal priceOfItemToBeRemoved = getPrice(itemId, cart);
                    logger.info("Subtracting the removed item's cost from the cart subtotal");
                    cart.setSubTotal(cart
                            .getSubTotal()
                            .subtract(getPrice(itemId, cart)));
                    logger.info("Removing the item from the cart");
                    cart
                            .getItems()
                            .removeIf(item -> item.bookId.equals(itemId));
                    ;
                    return cartRepository.upsertCart(cart);
                })
                .then();

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

}
