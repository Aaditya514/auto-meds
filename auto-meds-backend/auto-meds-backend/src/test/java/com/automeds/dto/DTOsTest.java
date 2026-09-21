package com.automeds.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class DTOsTest {

    @Test
    void testAddToCartRequest() {
        AddToCartRequest request = new AddToCartRequest();
        request.setMedicineId(1L);
        request.setQuantity(2);

        assertEquals(1L, request.getMedicineId());
        assertEquals(2, request.getQuantity());
        assertNotNull(request.toString());

        AddToCartRequest req2 = new AddToCartRequest(5L, 10);
        assertEquals(5L, req2.getMedicineId());
        assertEquals(10, req2.getQuantity());
    }

    @Test
    void testAdminDashboardDTO() {
        AdminDashboardDTO dto = new AdminDashboardDTO();
        dto.setTotalPatients(10L);
        dto.setTotalMedicines(20L);
        dto.setActiveSubscriptions(5L);
        dto.setPendingRequests(3L);
        dto.setUpcomingRefills(2L);
        dto.setPendingOrders(4L);
        dto.setLowStockMedicines(1L);
        dto.setOutOfStockMedicines(0L);

        assertEquals(10L, dto.getTotalPatients());
        assertEquals(20L, dto.getTotalMedicines());
        assertEquals(5L, dto.getActiveSubscriptions());
        assertEquals(3L, dto.getPendingRequests());
        assertEquals(2L, dto.getUpcomingRefills());
        assertEquals(4L, dto.getPendingOrders());
        assertEquals(1L, dto.getLowStockMedicines());
        assertEquals(0L, dto.getOutOfStockMedicines());
        assertNotNull(dto.toString());

        AdminDashboardDTO dto2 = new AdminDashboardDTO(15L, 25L, 6L, 4L, 3L, 5L, 2L, 1L);
        assertEquals(15L, dto2.getTotalPatients());
    }

    @Test
    void testAuthRequest() {
        AuthRequest req = new AuthRequest();
        req.setEmail("test@example.com");
        req.setPassword("pass123");

        assertEquals("test@example.com", req.getEmail());
        assertEquals("pass123", req.getPassword());
        assertNotNull(req.toString());

        AuthRequest req2 = new AuthRequest("a@b.com", "secret");
        assertEquals("a@b.com", req2.getEmail());
        assertEquals("secret", req2.getPassword());
    }

    @Test
    void testAuthResponse() {
        AuthResponse res = new AuthResponse();
        res.setToken("token123");
        res.setTokenType("Bearer");
        res.setUserId(1L);
        res.setName("Test User");
        res.setEmail("test@example.com");
        res.setRole("PATIENT");

        assertEquals("token123", res.getToken());
        assertEquals("Bearer", res.getTokenType());
        assertEquals(1L, res.getUserId());
        assertEquals("Test User", res.getName());
        assertEquals("test@example.com", res.getEmail());
        assertEquals("PATIENT", res.getRole());
        assertNotNull(res.toString());

        AuthResponse res2 = new AuthResponse("token456", 2L, "New Name", "new@example.com", "ADMIN");
        assertEquals("token456", res2.getToken());
    }

    @Test
    void testCartDTO() {
        CartDTO dto = new CartDTO();
        dto.setId(1L);
        dto.setPatientId(2L);
        dto.setItems(Collections.emptyList());
        dto.setTotalItems(0);
        dto.setSubtotal(BigDecimal.ZERO);
        dto.setDeliveryCharge(new BigDecimal("40.00"));
        dto.setTotalAmount(new BigDecimal("40.00"));

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getPatientId());
        assertTrue(dto.getItems().isEmpty());
        assertEquals(0, dto.getTotalItems());
        assertEquals(BigDecimal.ZERO, dto.getSubtotal());
        assertEquals(new BigDecimal("40.00"), dto.getDeliveryCharge());
        assertEquals(new BigDecimal("40.00"), dto.getTotalAmount());
        assertNotNull(dto.toString());

        CartDTO dto2 = new CartDTO(1L, 2L, Collections.emptyList(), 2, BigDecimal.TEN, new BigDecimal("40.00"), new BigDecimal("50.00"));
        assertEquals(new BigDecimal("50.00"), dto2.getTotalAmount());
    }

    @Test
    void testCartItemDTO() {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(1L);
        dto.setMedicineId(2L);
        dto.setMedicineName("Paracetamol");
        dto.setBrandName("ABC");
        dto.setComposition("Paracetamol");
        dto.setStrength("500mg");
        dto.setPrice(BigDecimal.valueOf(50.0));
        dto.setQuantity(2);
        dto.setSubtotal(BigDecimal.valueOf(100.0));
        dto.setInStock(true);

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getMedicineId());
        assertEquals("Paracetamol", dto.getMedicineName());
        assertEquals("ABC", dto.getBrandName());
        assertEquals("Paracetamol", dto.getComposition());
        assertEquals("500mg", dto.getStrength());
        assertEquals(BigDecimal.valueOf(50.0), dto.getPrice());
        assertEquals(2, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(100.0), dto.getSubtotal());
        assertTrue(dto.getInStock());
        assertNotNull(dto.toString());

        CartItemDTO dto2 = new CartItemDTO(1L, 2L, "Para", "ABC", "Comp", "500mg", BigDecimal.valueOf(50.0), 2, BigDecimal.valueOf(100.0), true);
        assertEquals("Para", dto2.getMedicineName());
    }

    @Test
    void testCheckoutRequest() {
        CheckoutRequest req = new CheckoutRequest();
        req.setDeliveryAddress("123 Street");
        req.setPaymentMethod("COD");

        assertEquals("123 Street", req.getDeliveryAddress());
        assertEquals("COD", req.getPaymentMethod());
        assertNotNull(req.toString());

        CheckoutRequest req2 = new CheckoutRequest("456 Avenue", "CARD");
        assertEquals("456 Avenue", req2.getDeliveryAddress());
    }

    @Test
    void testMedicineDTO() {
        MedicineDTO dto = new MedicineDTO();
        dto.setId(1L);
        dto.setMedicineName("Aspirin");
        dto.setBrandName("Bayer");
        dto.setComposition("Acetylsalicylic acid");
        dto.setStrength("100mg");
        dto.setCategory("Pain");
        dto.setPrice(BigDecimal.valueOf(20.0));
        dto.setStockQuantity(50);
        dto.setRequiresPrescription(false);
        dto.setDescription("Pain reliever");
        dto.setManufacturer("Bayer AG");
        LocalDateTime now = LocalDateTime.now();
        dto.setExpiryDate(now);
        dto.setActive(true);
        dto.setInStock(true);

        assertEquals(1L, dto.getId());
        assertEquals("Aspirin", dto.getMedicineName());
        assertEquals("Bayer", dto.getBrandName());
        assertEquals("Acetylsalicylic acid", dto.getComposition());
        assertEquals("100mg", dto.getStrength());
        assertEquals("Pain", dto.getCategory());
        assertEquals(BigDecimal.valueOf(20.0), dto.getPrice());
        assertEquals(50, dto.getStockQuantity());
        assertFalse(dto.getRequiresPrescription());
        assertEquals("Pain reliever", dto.getDescription());
        assertEquals("Bayer AG", dto.getManufacturer());
        assertEquals(now, dto.getExpiryDate());
        assertTrue(dto.getActive());
        assertTrue(dto.getInStock());
        assertNotNull(dto.toString());

        // Test getInStock fallback logic
        MedicineDTO dto2 = new MedicineDTO();
        dto2.setStockQuantity(10);
        assertTrue(dto2.getInStock());

        MedicineDTO dto3 = new MedicineDTO();
        dto3.setStockQuantity(0);
        assertFalse(dto3.getInStock());

        MedicineDTO dto4 = new MedicineDTO(1L, "Med", "Brand", "Comp", "100mg", "Cat", BigDecimal.TEN, 10, true, "Desc", "Mfg", now, true);
        assertTrue(dto4.getRequiresPrescription());
    }

    @Test
    void testNotificationDTO() {
        LocalDateTime now = LocalDateTime.now();
        NotificationDTO dto = new NotificationDTO();
        dto.setId(1L);
        dto.setUserId(2L);
        dto.setTitle("Title");
        dto.setMessage("Message");
        dto.setType("INFO");
        dto.setIsRead(false);
        dto.setCreatedAt(now);

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getUserId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Message", dto.getMessage());
        assertEquals("INFO", dto.getType());
        assertFalse(dto.getIsRead());
        assertEquals(now, dto.getCreatedAt());
        assertNotNull(dto.toString());

        NotificationDTO dto2 = new NotificationDTO(1L, 2L, "Title", "Message", "INFO", false, now);
        assertEquals(1L, dto2.getId());
    }

    @Test
    void testOrderDTO() {
        LocalDateTime now = LocalDateTime.now();
        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setPatientId(2L);
        dto.setPatientName("John");
        dto.setPatientEmail("john@test.com");
        dto.setSubscriptionId(3L);
        dto.setOrderDate(now);
        dto.setTotalAmount(BigDecimal.valueOf(200.0));
        dto.setDeliveryAddress("Address");
        dto.setPaymentMethod("COD");
        dto.setPaymentStatus("PAID");
        dto.setOrderStatus("DELIVERED");
        dto.setOrderType("ONE_TIME");
        dto.setExpectedDeliveryDate(now);
        dto.setItems(Collections.emptyList());

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getPatientId());
        assertEquals("John", dto.getPatientName());
        assertEquals("john@test.com", dto.getPatientEmail());
        assertEquals(3L, dto.getSubscriptionId());
        assertEquals(now, dto.getOrderDate());
        assertEquals(BigDecimal.valueOf(200.0), dto.getTotalAmount());
        assertEquals("Address", dto.getDeliveryAddress());
        assertEquals("COD", dto.getPaymentMethod());
        assertEquals("PAID", dto.getPaymentStatus());
        assertEquals("DELIVERED", dto.getOrderStatus());
        assertEquals("ONE_TIME", dto.getOrderType());
        assertEquals(now, dto.getExpectedDeliveryDate());
        assertTrue(dto.getItems().isEmpty());
        assertNotNull(dto.toString());

        OrderDTO dto2 = new OrderDTO(1L, 2L, "John", "john@test.com", 3L, now, BigDecimal.valueOf(200.0), "Address", "COD", "PAID", "DELIVERED", "ONE_TIME", now, Collections.emptyList());
        assertEquals("John", dto2.getPatientName());
    }

    @Test
    void testOrderItemDTO() {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(1L);
        dto.setMedicineId(2L);
        dto.setMedicineName("Para");
        dto.setBrandName("ABC");
        dto.setComposition("Comp");
        dto.setStrength("500mg");
        dto.setPrice(BigDecimal.valueOf(10.0));
        dto.setQuantity(2);
        dto.setSubtotal(BigDecimal.valueOf(20.0));

        assertEquals(1L, dto.getId());
        assertEquals(2L, dto.getMedicineId());
        assertEquals("Para", dto.getMedicineName());
        assertEquals("ABC", dto.getBrandName());
        assertEquals("Comp", dto.getComposition());
        assertEquals("500mg", dto.getStrength());
        assertEquals(2, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(10.0), dto.getPrice());
        assertEquals(BigDecimal.valueOf(20.0), dto.getSubtotal());
        assertNotNull(dto.toString());

        OrderItemDTO dto2 = new OrderItemDTO(1L, 2L, "Para", "ABC", "Comp", "500mg", 2, BigDecimal.valueOf(10.0), BigDecimal.valueOf(20.0));
        assertEquals(1L, dto2.getId());
    }

    @Test
    void testRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setName("User");
        req.setEmail("u@example.com");
        req.setPassword("p");
        req.setPhone("123");
        req.setAddress("Addr");
        req.setCity("City");
        req.setState("State");
        req.setPincode("111");

        assertEquals("User", req.getName());
        assertEquals("u@example.com", req.getEmail());
        assertEquals("p", req.getPassword());
        assertEquals("123", req.getPhone());
        assertEquals("Addr", req.getAddress());
        assertEquals("City", req.getCity());
        assertEquals("State", req.getState());
        assertEquals("111", req.getPincode());
        assertNotNull(req.toString());

        RegisterRequest req2 = new RegisterRequest("User", "u@example.com", "p", "123", "Addr", "City", "State", "111");
        assertEquals("User", req2.getName());
    }

    @Test
    void testSubscriptionRequestDTO() {
        SubscriptionRequestDTO req = new SubscriptionRequestDTO();
        req.setMedicineId(1L);
        req.setDosage("1 tablet");
        req.setFrequency("Daily");
        req.setQuantity(30);

        assertEquals(1L, req.getMedicineId());
        assertEquals("1 tablet", req.getDosage());
        assertEquals("Daily", req.getFrequency());
        assertEquals(30, req.getQuantity());
        assertNotNull(req.toString());

        SubscriptionRequestDTO req2 = new SubscriptionRequestDTO(1L, "1 tablet", "Daily", 30);
        assertEquals(1L, req2.getMedicineId());
    }

    @Test
    void testSubscriptionResponseDTO() {
        LocalDateTime now = LocalDateTime.now();
        SubscriptionResponseDTO res = new SubscriptionResponseDTO();
        res.setId(1L);
        res.setPatientId(2L);
        res.setPatientName("User");
        res.setPatientEmail("email");
        res.setMedicineId(3L);
        res.setMedicineName("Med");
        res.setBrandName("Brand");
        res.setComposition("Comp");
        res.setStrength("10mg");
        res.setPrescriptionId(4L);
        res.setPrescriptionFileName("file.pdf");
        res.setPrescriptionExpiryDate(now);
        res.setDosage("1 tab");
        res.setFrequency("Daily");
        res.setQuantity(30);
        res.setStartDate(now);
        res.setNextRefillDate(now);
        res.setNextDispatchDate(now);
        res.setStatus("ACTIVE");
        res.setCreatedAt(now);

        assertEquals(1L, res.getId());
        assertEquals(2L, res.getPatientId());
        assertEquals("User", res.getPatientName());
        assertEquals("email", res.getPatientEmail());
        assertEquals(3L, res.getMedicineId());
        assertEquals("Med", res.getMedicineName());
        assertEquals("Brand", res.getBrandName());
        assertEquals("Comp", res.getComposition());
        assertEquals("10mg", res.getStrength());
        assertEquals(4L, res.getPrescriptionId());
        assertEquals("file.pdf", res.getPrescriptionFileName());
        assertEquals(now, res.getPrescriptionExpiryDate());
        assertEquals("1 tab", res.getDosage());
        assertEquals("Daily", res.getFrequency());
        assertEquals(30, res.getQuantity());
        assertEquals(now, res.getStartDate());
        assertEquals(now, res.getNextRefillDate());
        assertEquals(now, res.getNextDispatchDate());
        assertEquals("ACTIVE", res.getStatus());
        assertEquals(now, res.getCreatedAt());
        assertNotNull(res.toString());

        SubscriptionResponseDTO res2 = new SubscriptionResponseDTO(1L, 2L, "User", "email", 3L, "Med", "Brand", "Comp", "10mg", 4L, "file.pdf", now, "1 tab", "Daily", 30, now, now, now, "ACTIVE", now);
        assertEquals(1L, res2.getId());
    }

    @Test
    void testUpdateCartItemRequest() {
        UpdateCartItemRequest req = new UpdateCartItemRequest();
        req.setQuantity(5);

        assertEquals(5, req.getQuantity());
        assertNotNull(req.toString());

        UpdateCartItemRequest req2 = new UpdateCartItemRequest(10);
        assertEquals(10, req2.getQuantity());
    }
}


