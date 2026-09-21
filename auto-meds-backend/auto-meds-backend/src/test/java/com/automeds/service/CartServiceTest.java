package com.automeds.service;

import com.automeds.dto.AddToCartRequest;
import com.automeds.dto.CartDTO;
import com.automeds.dto.UpdateCartItemRequest;
import com.automeds.entity.Cart;
import com.automeds.entity.CartItem;
import com.automeds.entity.Medicine;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.exception.InsufficientStockException;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.CartItemRepository;
import com.automeds.repository.CartRepository;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class CartServiceTest {

    private CartRepository cartRepository;
    private CartItemRepository cartItemRepository;
    private MedicineRepository medicineRepository;
    private UserRepository userRepository;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartRepository = Mockito.mock(CartRepository.class);
        cartItemRepository = Mockito.mock(CartItemRepository.class);
        medicineRepository = Mockito.mock(MedicineRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        cartService = new CartService(cartRepository, cartItemRepository, medicineRepository, userRepository);
    }

    @Test
    void testGetOrCreateCartNewCart() {
        User u = new User();
        u.setId(1L);

        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.empty());
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        Cart newCart = new Cart();
        newCart.setId(10L);
        newCart.setPatient(u);
        newCart.setItems(new ArrayList<>());
        Mockito.when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        Cart cart = cartService.getOrCreateCartForPatient(1L);
        assertNotNull(cart);
        assertEquals(10L, cart.getId());
    }

    @Test
    void testGetOrCreateCartUserNotFound() {
        Mockito.when(cartRepository.findByPatientId(99L)).thenReturn(Optional.empty());
        Mockito.when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cartService.getOrCreateCartForPatient(99L));
    }

    @Test
    void testAddItemToCartSuccessNewItem() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);
        cart.setItems(new ArrayList<>());

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(50);
        m.setPrice(BigDecimal.valueOf(100.0));

        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));
        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        Mockito.when(cartItemRepository.findByCartIdAndMedicineId(10L, 2L)).thenReturn(Optional.empty());

        AddToCartRequest req = new AddToCartRequest(2L, 2);
        CartDTO res = cartService.addItemToCart(1L, req);

        assertNotNull(res);
        Mockito.verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void testAddItemToCartSuccessExistingItem() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);
        cart.setItems(new ArrayList<>());

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(50);
        m.setPrice(BigDecimal.valueOf(100.0));

        CartItem existing = new CartItem();
        existing.setId(100L);
        existing.setCart(cart);
        existing.setMedicine(m);
        existing.setQuantity(2);

        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));
        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        Mockito.when(cartItemRepository.findByCartIdAndMedicineId(10L, 2L)).thenReturn(Optional.of(existing));

        AddToCartRequest req = new AddToCartRequest(2L, 3);
        CartDTO res = cartService.addItemToCart(1L, req);

        assertNotNull(res);
        assertEquals(5, existing.getQuantity());
        Mockito.verify(cartItemRepository).save(existing);
    }

    @Test
    void testAddItemToCartInactiveMedicine() {
        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(0);

        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));

        AddToCartRequest req = new AddToCartRequest(2L, 2);
        assertThrows(BadRequestException.class, () -> cartService.addItemToCart(1L, req));
    }

    @Test
    void testAddItemToCartOutOfStock() {
        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(0);

        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));

        AddToCartRequest req = new AddToCartRequest(2L, 2);
        assertThrows(InsufficientStockException.class, () -> cartService.addItemToCart(1L, req));
    }

    @Test
    void testAddItemToCartExceedsStock() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(5);

        CartItem existing = new CartItem();
        existing.setQuantity(4);

        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));
        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        Mockito.when(cartItemRepository.findByCartIdAndMedicineId(10L, 2L)).thenReturn(Optional.of(existing));

        AddToCartRequest req = new AddToCartRequest(2L, 2);
        assertThrows(InsufficientStockException.class, () -> cartService.addItemToCart(1L, req));
    }

    @Test
    void testUpdateCartItemQuantitySuccess() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);
        cart.setItems(new ArrayList<>());

        Medicine m = new Medicine();
        m.setStockQuantity(50);
        m.setPrice(BigDecimal.valueOf(20.0));

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(cart);
        item.setMedicine(m);
        item.setQuantity(2);

        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        Mockito.when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        UpdateCartItemRequest req = new UpdateCartItemRequest();
        req.setQuantity(5);

        CartDTO dto = cartService.updateCartItemQuantity(1L, 100L, req);
        assertNotNull(dto);
        Mockito.verify(cartItemRepository).save(item);
    }

    @Test
    void testUpdateCartItemQuantityExceedsStock() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);

        Medicine m = new Medicine();
        m.setStockQuantity(50);

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(cart);
        item.setMedicine(m);

        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        Mockito.when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        UpdateCartItemRequest req = new UpdateCartItemRequest(6);
        m.setStockQuantity(4);
        assertThrows(InsufficientStockException.class, () -> cartService.updateCartItemQuantity(1L, 100L, req));
    }

    @Test
    void testAddItemToCartExceedsMaxQuantityLimit() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);

        Medicine m = new Medicine();
        m.setId(2L);
        m.setActive(1);
        m.setStockQuantity(50);

        Mockito.when(medicineRepository.findById(2L)).thenReturn(Optional.of(m));
        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        Mockito.when(cartItemRepository.findByCartIdAndMedicineId(10L, 2L)).thenReturn(Optional.empty());

        AddToCartRequest req = new AddToCartRequest(2L, 15);
        assertThrows(BadRequestException.class, () -> cartService.addItemToCart(1L, req));
    }

    @Test
    void testUpdateCartItemQuantityExceedsMaxQuantityLimit() {
        UpdateCartItemRequest req = new UpdateCartItemRequest(15);
        assertThrows(BadRequestException.class, () -> cartService.updateCartItemQuantity(1L, 100L, req));
    }

    @Test
    void testUpdateCartItemQuantityUnauthorized() {
        User u = new User();
        u.setId(1L);

        Cart cart1 = new Cart();
        cart1.setId(10L);
        cart1.setPatient(u);

        Cart cart2 = new Cart();
        cart2.setId(20L);

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(cart2);

        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart1));
        Mockito.when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        UpdateCartItemRequest req = new UpdateCartItemRequest();
        req.setQuantity(5);

        assertThrows(BadRequestException.class, () -> cartService.updateCartItemQuantity(1L, 100L, req));
    }

    @Test
    void testRemoveCartItemSuccess() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);
        cart.setItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(cart);

        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));
        Mockito.when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        CartDTO dto = cartService.removeCartItem(1L, 100L);
        assertNotNull(dto);
        Mockito.verify(cartItemRepository).delete(item);
    }

    @Test
    void testRemoveCartItemUnauthorized() {
        User u = new User();
        u.setId(1L);

        Cart cart1 = new Cart();
        cart1.setId(10L);
        cart1.setPatient(u);

        Cart cart2 = new Cart();
        cart2.setId(20L);

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(cart2);

        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart1));
        Mockito.when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class, () -> cartService.removeCartItem(1L, 100L));
    }

    @Test
    void testConvertToDTOWithOutOfStockItem() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);

        Medicine outOfStockMed = new Medicine();
        outOfStockMed.setId(5L);
        outOfStockMed.setMedicineName("Med");
        outOfStockMed.setPrice(BigDecimal.TEN);
        outOfStockMed.setStockQuantity(0);

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(cart);
        item.setMedicine(outOfStockMed);
        item.setQuantity(2);

        cart.setItems(Collections.singletonList(item));

        CartDTO dto = cartService.convertToDTO(cart);
        assertNotNull(dto);
        assertFalse(dto.getItems().get(0).getInStock());
    }

    @Test
    void testClearCart() {
        User u = new User();
        u.setId(1L);

        Cart cart = new Cart();
        cart.setId(10L);
        cart.setPatient(u);

        Mockito.when(cartRepository.findByPatientId(1L)).thenReturn(Optional.of(cart));

        cartService.clearCart(1L);
        Mockito.verify(cartItemRepository).deleteByCartId(10L);
    }
}


