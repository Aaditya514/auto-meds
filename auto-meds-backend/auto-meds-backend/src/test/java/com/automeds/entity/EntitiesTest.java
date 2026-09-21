package com.automeds.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class EntitiesTest {

    @Test
    void testUserEntity() {
        User u = new User();
        u.setId(1L);
        u.setName("John");
        u.setEmail("john@example.com");
        u.setPassword("pass");
        u.setRole("PATIENT");
        u.setPhone("123");
        u.setAddress("Addr");
        u.setCity("City");
        u.setState("State");
        u.setPincode("111");
        LocalDateTime now = LocalDateTime.now();
        u.setCreatedAt(now);

        assertEquals(1L, u.getId());
        assertEquals("John", u.getName());
        assertEquals("john@example.com", u.getEmail());
        assertEquals("pass", u.getPassword());
        assertEquals("PATIENT", u.getRole());
        assertEquals("123", u.getPhone());
        assertEquals("Addr", u.getAddress());
        assertEquals("City", u.getCity());
        assertEquals("State", u.getState());
        assertEquals("111", u.getPincode());
        assertEquals(now, u.getCreatedAt());
        assertNotNull(u.toString());

        User u2 = new User(2L, "Jane", "jane@example.com", "pass2", null, "999", "A2", "C2", "S2", "222");
        assertEquals("Jane", u2.getName());
        assertEquals("PATIENT", u2.getRole());

        User u3 = new User();
        u3.setCreatedAt(null);
        u3.onCreate();
        assertNotNull(u3.getCreatedAt());
    }

    @Test
    void testMedicineEntity() {
        Medicine m = new Medicine();
        m.setId(1L);
        m.setMedicineName("Med");
        m.setBrandName("Brand");
        m.setComposition("Comp");
        m.setStrength("10mg");
        m.setCategory("Cat");
        m.setPrice(BigDecimal.valueOf(10.5));
        m.setStockQuantity(100);
        m.setRequiresPrescription(1);
        m.setDescription("Desc");
        m.setManufacturer("Mfg");
        LocalDateTime now = LocalDateTime.now();
        m.setExpiryDate(now);
        m.setActive(1);

        assertEquals(1L, m.getId());
        assertEquals("Med", m.getMedicineName());
        assertEquals("Brand", m.getBrandName());
        assertEquals("Comp", m.getComposition());
        assertEquals("10mg", m.getStrength());
        assertEquals("Cat", m.getCategory());
        assertEquals(BigDecimal.valueOf(10.5), m.getPrice());
        assertEquals(100, m.getStockQuantity());
        assertEquals(1, m.getRequiresPrescription());
        assertEquals("Desc", m.getDescription());
        assertEquals("Mfg", m.getManufacturer());
        assertEquals(now, m.getExpiryDate());
        assertEquals(1, m.getActive());
        assertNotNull(m.toString());

        Medicine m2 = new Medicine(2L, "Med2", "Brand2", "Comp2", "20mg", "Cat2", BigDecimal.TEN, 50, null, "Desc2", "Mfg2", now, null);
        assertEquals(0, m2.getRequiresPrescription());
        assertEquals(1, m2.getActive());
    }

    @Test
    void testCartAndCartItemEntity() {
        User u = new User();
        u.setId(1L);

        Cart c = new Cart();
        c.setId(10L);
        c.setPatient(u);
        c.setItems(new ArrayList<>());
        LocalDateTime now = LocalDateTime.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);

        assertEquals(10L, c.getId());
        assertEquals(u, c.getPatient());
        assertTrue(c.getItems().isEmpty());
        assertEquals(now, c.getCreatedAt());
        assertEquals(now, c.getUpdatedAt());
        assertNotNull(c.toString());

        Cart c2 = new Cart(20L, u);
        assertEquals(u, c2.getPatient());

        Cart c3 = new Cart();
        c3.onCreate();
        c3.onUpdate();
        assertNotNull(c3.getCreatedAt());

        Cart nullPatientCart = new Cart();
        assertNotNull(nullPatientCart.toString());

        Medicine m = new Medicine();
        m.setId(5L);

        CartItem item = new CartItem();
        item.setId(100L);
        item.setCart(c);
        item.setMedicine(m);
        item.setQuantity(3);
        item.setPriceAtAddition(BigDecimal.valueOf(15.0));

        assertEquals(100L, item.getId());
        assertEquals(c, item.getCart());
        assertEquals(m, item.getMedicine());
        assertEquals(3, item.getQuantity());
        assertEquals(BigDecimal.valueOf(15.0), item.getPriceAtAddition());
        assertNotNull(item.toString());

        CartItem item2 = new CartItem(101L, c, m, 2, BigDecimal.valueOf(20.0));
        assertEquals(2, item2.getQuantity());

        CartItem nullMedItem = new CartItem();
        assertNotNull(nullMedItem.toString());
    }

    @Test
    void testPrescriptionEntity() {
        User u = new User();
        u.setId(1L);

        Prescription p = new Prescription();
        p.setId(1L);
        p.setPatient(u);
        p.setFileName("doc.pdf");
        p.setFilePath("uploads/doc.pdf");
        LocalDateTime now = LocalDateTime.now();
        p.setUploadDate(now);
        p.setExpiryDate(now.plusMonths(6));
        p.setStatus("PENDING");

        assertEquals(1L, p.getId());
        assertEquals(u, p.getPatient());
        assertEquals("doc.pdf", p.getFileName());
        assertEquals("uploads/doc.pdf", p.getFilePath());
        assertEquals(now, p.getUploadDate());
        assertEquals(now.plusMonths(6), p.getExpiryDate());
        assertEquals("PENDING", p.getStatus());
        assertNotNull(p.toString());

        Prescription p2 = new Prescription(2L, u, "f.pdf", "p.pdf", null, now, null);
        assertEquals("PENDING", p2.getStatus());
        assertNotNull(p2.getUploadDate());

        Prescription p3 = new Prescription();
        p3.setUploadDate(null);
        p3.setStatus(null);
        p3.onCreate();
        assertEquals("PENDING", p3.getStatus());
        assertNotNull(p3.getUploadDate());

        Prescription nullPatientP = new Prescription();
        assertNotNull(nullPatientP.toString());
    }

    @Test
    void testSubscriptionEntity() {
        User u = new User();
        u.setId(1L);
        Medicine m = new Medicine();
        m.setId(2L);
        Prescription p = new Prescription();

        Subscription s = new Subscription();
        s.setId(1L);
        s.setPatient(u);
        s.setMedicine(m);
        s.setPrescription(p);
        s.setDosage("1 tab");
        s.setFrequency("Daily");
        s.setQuantity(30);
        LocalDateTime now = LocalDateTime.now();
        s.setStartDate(now);
        s.setNextRefillDate(now.plusDays(30));
        s.setNextDispatchDate(now.plusDays(28));
        s.setStatus("ACTIVE");
        s.setCreatedAt(now);
        s.setUpdatedAt(now);

        assertEquals(1L, s.getId());
        assertEquals(u, s.getPatient());
        assertEquals(m, s.getMedicine());
        assertEquals(p, s.getPrescription());
        assertEquals("1 tab", s.getDosage());
        assertEquals("Daily", s.getFrequency());
        assertEquals(30, s.getQuantity());
        assertEquals(now, s.getStartDate());
        assertEquals(now.plusDays(30), s.getNextRefillDate());
        assertEquals(now.plusDays(28), s.getNextDispatchDate());
        assertEquals("ACTIVE", s.getStatus());
        assertEquals(now, s.getCreatedAt());
        assertEquals(now, s.getUpdatedAt());
        assertNotNull(s.toString());

        Subscription s2 = new Subscription(2L, u, m, p, "2 tabs", "Weekly", 60, now, now, now, null);
        assertEquals("PENDING", s2.getStatus());

        Subscription s3 = new Subscription();
        s3.setCreatedAt(null);
        s3.setStatus(null);
        s3.onCreate();
        s3.onUpdate();
        assertEquals("PENDING", s3.getStatus());
        assertNotNull(s3.getCreatedAt());

        Subscription nullRefsSub = new Subscription();
        assertNotNull(nullRefsSub.toString());
    }

    @Test
    void testOrderAndOrderItemEntity() {
        User u = new User();
        u.setId(1L);
        Subscription s = new Subscription();

        Order o = new Order();
        o.setId(1L);
        o.setPatient(u);
        o.setSubscription(s);
        LocalDateTime now = LocalDateTime.now();
        o.setOrderDate(now);
        o.setTotalAmount(BigDecimal.valueOf(150.0));
        o.setDeliveryAddress("123 Main St");
        o.setPaymentMethod("COD");
        o.setPaymentStatus("PENDING");
        o.setOrderStatus("PENDING");
        o.setOrderType("AUTO_REFILL");
        o.setExpectedDeliveryDate(now.plusDays(3));
        o.setItems(new ArrayList<>());

        assertEquals(1L, o.getId());
        assertEquals(u, o.getPatient());
        assertEquals(s, o.getSubscription());
        assertEquals(now, o.getOrderDate());
        assertEquals(BigDecimal.valueOf(150.0), o.getTotalAmount());
        assertEquals("123 Main St", o.getDeliveryAddress());
        assertEquals("COD", o.getPaymentMethod());
        assertEquals("PENDING", o.getPaymentStatus());
        assertEquals("PENDING", o.getOrderStatus());
        assertEquals("AUTO_REFILL", o.getOrderType());
        assertEquals(now.plusDays(3), o.getExpectedDeliveryDate());
        assertTrue(o.getItems().isEmpty());
        assertNotNull(o.toString());

        Order o2 = new Order(2L, u, s, null, BigDecimal.TEN, "Addr", null, null, null, null, now);
        assertEquals("CASH_ON_DELIVERY", o2.getPaymentMethod());
        assertEquals("PENDING", o2.getPaymentStatus());
        assertEquals("PENDING", o2.getOrderStatus());
        assertEquals("ONE_TIME", o2.getOrderType());

        Order o3 = new Order();
        o3.setOrderDate(null);
        o3.setOrderStatus(null);
        o3.setPaymentStatus(null);
        o3.setOrderType(null);
        o3.onCreate();
        assertEquals("PENDING", o3.getOrderStatus());
        assertEquals("PENDING", o3.getPaymentStatus());
        assertEquals("ONE_TIME", o3.getOrderType());

        Order nullUserOrder = new Order();
        assertNotNull(nullUserOrder.toString());

        Medicine m = new Medicine();
        m.setId(10L);

        OrderItem item = new OrderItem();
        item.setId(10L);
        item.setOrder(o);
        item.setMedicine(m);
        item.setQuantity(2);
        item.setPrice(BigDecimal.valueOf(50.0));
        item.setSubtotal(BigDecimal.valueOf(100.0));

        assertEquals(10L, item.getId());
        assertEquals(o, item.getOrder());
        assertEquals(m, item.getMedicine());
        assertEquals(2, item.getQuantity());
        assertEquals(BigDecimal.valueOf(50.0), item.getPrice());
        assertEquals(BigDecimal.valueOf(100.0), item.getSubtotal());
        assertNotNull(item.toString());

        OrderItem item2 = new OrderItem(11L, o, m, 1, BigDecimal.valueOf(30.0), BigDecimal.valueOf(30.0));
        assertEquals(BigDecimal.valueOf(30.0), item2.getSubtotal());

        OrderItem nullMedOrderItem = new OrderItem();
        assertNotNull(nullMedOrderItem.toString());
    }

    @Test
    void testNotificationEntity() {
        User u = new User();
        u.setId(1L);

        Notification n = new Notification();
        n.setId(1L);
        n.setUser(u);
        n.setTitle("Title");
        n.setMessage("Msg");
        n.setType("INFO");
        n.setIsRead(0);
        LocalDateTime now = LocalDateTime.now();
        n.setCreatedAt(now);

        assertEquals(1L, n.getId());
        assertEquals(u, n.getUser());
        assertEquals("Title", n.getTitle());
        assertEquals("Msg", n.getMessage());
        assertEquals("INFO", n.getType());
        assertEquals(0, n.getIsRead());
        assertEquals(now, n.getCreatedAt());
        assertNotNull(n.toString());

        Notification n2 = new Notification(2L, u, "T2", "M2", null, null);
        assertEquals("INFO", n2.getType());
        assertEquals(0, n2.getIsRead());

        Notification n3 = new Notification();
        n3.setCreatedAt(null);
        n3.setIsRead(null);
        n3.onCreate();
        assertEquals(0, n3.getIsRead());
        assertNotNull(n3.getCreatedAt());

        Notification nullUserNotif = new Notification();
        assertNotNull(nullUserNotif.toString());
    }
}


