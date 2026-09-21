package com.automeds.config;

import com.automeds.entity.Cart;
import com.automeds.entity.Medicine;
import com.automeds.entity.User;
import com.automeds.repository.CartRepository;
import com.automeds.repository.MedicineRepository;
import com.automeds.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            CartRepository cartRepository,
            MedicineRepository medicineRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Seed Admin User
            if (!userRepository.existsByEmail("admin@automeds.com")) {
                User admin = new User();
                admin.setName("System Admin");
                admin.setEmail("admin@automeds.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                admin.setPhone("9999999999");
                admin.setAddress("Admin Head Office");
                admin.setCity("Metropolis");
                admin.setState("Central");
                admin.setPincode("100001");
                userRepository.save(admin);
                log.info(">>> Seeded Demo Admin Account: admin@automeds.com / admin123");
            }

            // Seed Patient User
            if (!userRepository.existsByEmail("patient@automeds.com")) {
                User patient = new User();
                patient.setName("Aaditya Aanand");
                patient.setEmail("patient@automeds.com");
                patient.setPassword(passwordEncoder.encode("patient123"));
                patient.setRole("PATIENT");
                patient.setPhone("9876543210");
                patient.setAddress("123 Health Ave, Suite 4B");
                patient.setCity("Greenville");
                patient.setState("Stateville");
                patient.setPincode("560001");
                User savedPatient = userRepository.save(patient);

                Cart cart = new Cart();
                cart.setPatient(savedPatient);
                cartRepository.save(cart);
                log.info(">>> Seeded Demo Patient Account: patient@automeds.com / patient123");
            }

            // Seed sample medicines if empty
            if (medicineRepository.count() == 0) {
                medicineRepository.save(new Medicine(null, "Metformin 500mg", "ABC Pharma", "Metformin", "500mg", "Diabetes", new BigDecimal("100.00"), 0, 1, "First-line medication for type 2 diabetes.", "ABC Pharmaceuticals", LocalDateTime.now().plusMonths(24), 1));
                medicineRepository.save(new Medicine(null, "Metformin 500mg", "XYZ Pharma", "Metformin", "500mg", "Diabetes", new BigDecimal("80.00"), 50, 1, "Metformin hydrochloride 500mg sustained release.", "XYZ Healthcare", LocalDateTime.now().plusMonths(18), 1));
                medicineRepository.save(new Medicine(null, "Metformin 500mg", "PQR Pharma", "Metformin", "500mg", "Diabetes", new BigDecimal("75.00"), 25, 1, "Metformin hydrochloride tablets 500mg.", "PQR Labs", LocalDateTime.now().plusMonths(20), 1));
                medicineRepository.save(new Medicine(null, "Metformin 1000mg", "ABC Pharma", "Metformin", "1000mg", "Diabetes", new BigDecimal("140.00"), 40, 1, "Higher dosage metformin 1000mg.", "ABC Pharmaceuticals", LocalDateTime.now().plusMonths(24), 1));
                medicineRepository.save(new Medicine(null, "Amlodipine 5mg", "ABC Pharma", "Amlodipine", "5mg", "Cardiovascular", new BigDecimal("50.00"), 100, 1, "Calcium channel blocker for high blood pressure.", "ABC Pharmaceuticals", LocalDateTime.now().plusMonths(12), 1));
                medicineRepository.save(new Medicine(null, "Amlodipine 5mg", "Sun Pharma", "Amlodipine", "5mg", "Cardiovascular", new BigDecimal("45.00"), 80, 1, "Amlodipine besylate tablets 5mg.", "Sun Pharma Industries", LocalDateTime.now().plusMonths(30), 1));
                medicineRepository.save(new Medicine(null, "Atorvastatin 10mg", "Cipla", "Atorvastatin", "10mg", "Cholesterol", new BigDecimal("110.00"), 60, 1, "Statin medication for cholesterol management.", "Cipla Ltd", LocalDateTime.now().plusMonths(15), 1));
                log.info(">>> Seeded Demo Medicines");
            }
        };
    }
}
