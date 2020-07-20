package com.ratella.store.model.cart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class DefaultCartServiceUnitTest {

    private CartRepository cartRepositoryMock;

    @BeforeEach
    void initAll() {
        cartRepositoryMock = mock(CartRepository.class);
    }

    @Test
    public void removeItemFromCartWithItemsTest() {

        List<CartItem> cartItems = new ArrayList<>();
        CartItem item1 = new CartItem();
        item1.setBookId("i1");
        item1.setPrice(new BigDecimal(40));
        cartItems.add(item1);
        CartItem item2 = new CartItem();
        item2.setBookId("i2");
        item2.setPrice(new BigDecimal(30));

        cartItems.add(item2);
        Cart cart1 = new Cart();
        cart1.setId("c1");
        cart1.setSubTotal(new BigDecimal(70));
        cart1.setItems(cartItems);


        when(cartRepositoryMock.getCartById("c1")).thenReturn(Mono.just(cart1));
        when(cartRepositoryMock.upsertCart(cart1)).thenReturn(Mono.just(200));
        when(cartRepositoryMock.deleteCart(any(), any())).thenReturn(Mono.just(200));

        CartService cartService = new DefaultCartService(cartRepositoryMock);
        cartService
                .removeItemFromCart("c1", "i1")
                .subscribe();
        assertThat(cart1.getSubTotal()).isEqualTo(new BigDecimal(30));
        assertThat(cart1
                .getItems()
                .size()).isEqualTo(1);
        verify(cartRepositoryMock, times(1)).upsertCart(cart1);
        verify(cartRepositoryMock, times(0)).deleteCart("", "");

    }

    @Test
    public void removeItemFromCartWithOneItemTest() {

        List<CartItem> cartItems = new ArrayList<>();
        CartItem item1 = new CartItem();
        item1.setBookId("i1");
        item1.setPrice(new BigDecimal(40));
        cartItems.add(item1);

        Cart cart1 = new Cart();
        cart1.setId("c1");
        cart1.setSubTotal(new BigDecimal(40));
        cart1.setItems(cartItems);

        when(cartRepositoryMock.getCartById("c1")).thenReturn(Mono.just(cart1));
        when(cartRepositoryMock.upsertCart(cart1)).thenReturn(Mono.just(200));
        when(cartRepositoryMock.deleteCart("c1", "c1")).thenReturn(Mono.just(200));
        CartService cartService = new DefaultCartService(cartRepositoryMock);
        cartService
                .removeItemFromCart("c1", "i1")
                .subscribe();
        verify(cartRepositoryMock, times(0)).upsertCart(cart1);
        verify(cartRepositoryMock, times(1)).deleteCart("c1", "c1");
    }

    @Test
    public void getNumberOfItemsInTheCartTest() {
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(new CartItem());
        cartItems.add(new CartItem());
        Cart cart1 = new Cart();
        cart1.setItems(cartItems);

        when(cartRepositoryMock.getCartById(any())).thenReturn(Mono.just(cart1));

        CartService cartService = new DefaultCartService(cartRepositoryMock);
        StepVerifier
                .create(cartService.getNumberOfItemsInTheCart("100"))
                .expectNext(2)
                .verifyComplete();

    }

    @Test
    public void addItemToANewCartTest(){
        Cart cart1 = new Cart();

        CartItem item1 = new CartItem();
        item1.setBookId("i1");
        item1.setPrice(new BigDecimal(40));
        when(cartRepositoryMock.getCartById("c1")).thenReturn(Mono.just(cart1));
        when(cartRepositoryMock.saveCart(cart1)).thenReturn(Mono.just(200));
        CartService cartService = new DefaultCartService(cartRepositoryMock);
        cartService.addItemToCart("c1",item1).subscribe();

        assertThat(cart1.getSubTotal()).isEqualTo(new BigDecimal(40));
        assertThat(cart1.getItems().size()).isEqualTo(1);
        verify(cartRepositoryMock,times(1)).saveCart(cart1);
        verify(cartRepositoryMock,times(0)).upsertCart(cart1);
    }

    @Test
    public void addItemToAnExistingCartTest(){
        CartItem item1 = new CartItem();
        item1.setBookId("i1");
        item1.setPrice(new BigDecimal(40));
        Cart cart1 = new Cart();
        cart1.setId("c1");
        cart1.setSubTotal(new BigDecimal(40));
        cart1.getItems().add(item1);

        CartItem item2 = new CartItem();
        item2.setBookId("i2");
        item2.setPrice(new BigDecimal(100));


        when(cartRepositoryMock.getCartById("c1")).thenReturn(Mono.just(cart1));
        when(cartRepositoryMock.upsertCart(cart1)).thenReturn(Mono.just(200));
        CartService cartService = new DefaultCartService(cartRepositoryMock);
        cartService.addItemToCart("c1",item2).subscribe();

        assertThat(cart1.getSubTotal()).isEqualTo(new BigDecimal(140));
        assertThat(cart1.getItems().size()).isEqualTo(2);
        verify(cartRepositoryMock,times(0)).saveCart(cart1);
        verify(cartRepositoryMock,times(1)).upsertCart(cart1);
    }


}
