package com.automeds.service;

import com.automeds.dto.AddToCartRequest;
import com.automeds.dto.CartDTO;
import com.automeds.dto.CartItemDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: CartService
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class CartService {

    public static final int MAX_QUANTITY_PER_ITEM = 10;
    public static final int MAX_CART_ITEMS = 20;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MedicineRepository medicineRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, MedicineRepository medicineRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.medicineRepository = medicineRepository;
        this.userRepository = userRepository;
    }

    public Cart getOrCreateCartForPatient(Long patientId) {
        return cartRepository.findByPatientId(patientId)
                .orElseGet(() -> {
                    User patient = userRepository.findById(patientId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", patientId));
                    Cart cart = new Cart();
                    cart.setPatient(patient);
                    return cartRepository.save(cart);
                });
    }

    public CartDTO getCartByPatientId(Long patientId) {
        Cart cart = getOrCreateCartForPatient(patientId);
        return convertToDTO(cart);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public CartDTO addItemToCart(Long patientId, AddToCartRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0.");
        }

        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicine", "id", request.getMedicineId()));

        if (medicine.getActive() == 0) {
            throw new BadRequestException("Medicine is currently inactive and cannot be added to cart.");
        }

        if (medicine.getAvailableQuantity() <= 0) {
            if (medicine.getReservedQuantity() > 0) {
                throw new InsufficientStockException("All current stock of '" + medicine.getMedicineName() + "' is locked and reserved for ongoing patient auto-refill subscriptions.");
            }
            throw new InsufficientStockException("Medicine is currently OUT OF STOCK.");
        }

        Cart cart = getOrCreateCartForPatient(patientId);
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndMedicineId(cart.getId(), medicine.getId());

        int currentCartQuantity = existingItemOpt.map(CartItem::getQuantity).orElse(0);
        int totalRequestedQuantity = currentCartQuantity + request.getQuantity();

        if (totalRequestedQuantity > MAX_QUANTITY_PER_ITEM) {
            throw new BadRequestException("Maximum allowed limit is " + MAX_QUANTITY_PER_ITEM + " units per medicine item per order.");
        }

        if (totalRequestedQuantity > medicine.getAvailableQuantity()) {
            throw new InsufficientStockException("Only " + medicine.getAvailableQuantity() + " units available for purchase (" 
                    + medicine.getReservedQuantity() + " units are reserved for ongoing patient subscriptions).");
        }

        if (existingItemOpt.isEmpty() && cart.getItems() != null && cart.getItems().size() >= MAX_CART_ITEMS) {
            throw new BadRequestException("Cart item limit reached (maximum " + MAX_CART_ITEMS + " different medicines per order).");
        }

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(totalRequestedQuantity);
            existingItem.setPriceAtAddition(medicine.getPrice());
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setMedicine(medicine);
            newItem.setQuantity(request.getQuantity());
            newItem.setPriceAtAddition(medicine.getPrice());
            cartItemRepository.save(newItem);
            if (cart.getItems() != null) {
                cart.getItems().add(newItem);
            }
        }

        return getCartByPatientId(patientId);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public CartDTO updateCartItemQuantity(Long patientId, Long itemId, UpdateCartItemRequest request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0.");
        }

        if (request.getQuantity() > MAX_QUANTITY_PER_ITEM) {
            throw new BadRequestException("Maximum allowed limit is " + MAX_QUANTITY_PER_ITEM + " units per medicine item per order.");
        }

        Cart cart = getOrCreateCartForPatient(patientId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Unauthorized access to cart item.");
        }

        Medicine medicine = cartItem.getMedicine();
        if (request.getQuantity() > medicine.getAvailableQuantity()) {
            throw new InsufficientStockException("Only " + medicine.getAvailableQuantity() + " units available for purchase (" 
                    + medicine.getReservedQuantity() + " units are reserved for ongoing patient subscriptions).");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return getCartByPatientId(patientId);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public CartDTO removeCartItem(Long patientId, Long itemId) {
        Cart cart = getOrCreateCartForPatient(patientId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Unauthorized access to cart item.");
        }

        // Remove from parent collection to satisfy JPA orphanRemoval
        if (cart.getItems() != null) {
            cart.getItems().removeIf(item -> item.getId() != null && item.getId().equals(itemId));
        }

        cartItemRepository.delete(cartItem);
        return convertToDTO(cart);
    }

    // Wraps execution inside a database transaction
    @Transactional
    public void clearCart(Long patientId) {
        Cart cart = getOrCreateCartForPatient(patientId);
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cartItemRepository.deleteByCartId(cart.getId());
    }

    public CartDTO convertToDTO(Cart cart) {
        List<CartItem> items = (cart.getItems() != null && !cart.getItems().isEmpty())
                ? cart.getItems()
                : (cart.getId() != null ? cartItemRepository.findByCartId(cart.getId()) : Collections.emptyList());
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        int totalItemsCount = 0;
        BigDecimal subtotalSum = BigDecimal.ZERO;

        for (CartItem item : items) {
            Medicine m = item.getMedicine();
            BigDecimal itemSubtotal = m.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotalSum = subtotalSum.add(itemSubtotal);
            totalItemsCount += item.getQuantity();

            CartItemDTO itemDTO = new CartItemDTO(
                    item.getId(),
                    m.getId(),
                    m.getMedicineName(),
                    m.getBrandName(),
                    m.getComposition(),
                    m.getStrength(),
                    m.getPrice(),
                    item.getQuantity(),
                    itemSubtotal,
                    m.getStockQuantity() > 0
            );
            itemDTOs.add(itemDTO);
        }

        BigDecimal deliveryCharge = totalItemsCount > 0 ? new BigDecimal("40.00") : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotalSum.add(deliveryCharge);

        return new CartDTO(
                cart.getId(),
                cart.getPatient().getId(),
                itemDTOs,
                totalItemsCount,
                subtotalSum,
                deliveryCharge,
                totalAmount
        );
    }
}
