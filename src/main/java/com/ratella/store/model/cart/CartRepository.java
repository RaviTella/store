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


    public Mono<Integer> getCartItemCount(String id) {
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setMaxBufferedItemCount(10);
        queryOptions.setPartitionKey(new PartitionKey(id));
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
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        queryOptions.setMaxBufferedItemCount(10);
        queryOptions.setPartitionKey(new PartitionKey(id));
        String query = "SELECT * FROM cart c WHERE c.id = " + "'" + id + "'";
        return cosmosDB
                .getContainer()
                .queryItems(
                        query, queryOptions, Cart.class)
                .byPage()
                .map(feedResponse -> {
                    Cart cart = feedResponse
                            .getResults()
                            //if there is no cart then return an empty cart, otherwise return the existing cart
                            //TODO how to avoid returning new Cart()??
                            .size() == 0 ? new Cart() : feedResponse
                            .getElements()
                            .stream()
                            .findFirst()
                            .get();
                    return cart;
                })
                .single();


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

    public Mono<Integer> deleteCartItem(String cartId, String itemId) {
        return cosmosDB
                .getContainer()
                .readItem(cartId, new PartitionKey(cartId), Cart.class)
                .map(CosmosItemResponse::getItem)
                .flatMap(cart -> {
                    //Delete cart abd return if there is only one item
                    if (cart.getItems().size()==1) return deleteCart(cartId,cartId);
                    logger.info("Getting price of the item to be deleted");
                    BigDecimal itemPrice = cart
                            .getItems()
                            .stream()
                            .filter(item -> item.bookId.equals(itemId))
                            .findFirst()
                            .get()
                            .getPrice();
                    logger.info("Removing the item from the cart");
                    cart
                            .getItems()
                            .removeIf(item -> item.bookId.equals(itemId));
                    logger.info("Subtracting the removed item's cost from the cart subtotal");
                    cart.setSubTotal(cart
                            .getSubTotal()
                            .subtract(itemPrice));
                    return upsertCart(cart);
                });

    }

    public Mono<Integer> deleteCart(String id, String partitionKey) {
        return cosmosDB
                .getContainer()
                .deleteItem(id, new PartitionKey(partitionKey))
                .map(CosmosItemResponse::getStatusCode);
    }


}
