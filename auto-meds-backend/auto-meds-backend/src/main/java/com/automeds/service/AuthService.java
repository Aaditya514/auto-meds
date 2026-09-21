package com.automeds.service;

import com.automeds.dto.AuthRequest;
import com.automeds.dto.AuthResponse;
import com.automeds.dto.RegisterRequest;
import com.automeds.entity.Cart;
import com.automeds.entity.User;
import com.automeds.exception.BadRequestException;
import com.automeds.repository.CartRepository;
import com.automeds.repository.UserRepository;
import com.automeds.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Mark this class as a Spring Service managed bean containing business logic
@Service
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: AuthService
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class AuthService {

    // Inject repositories and utility beans using constructor injection
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, CartRepository cartRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    // Execute in a database transaction to ensure atomicity (all-or-nothing rollback)
    // Wraps execution inside a database transaction
    @Transactional
    public AuthResponse registerPatient(RegisterRequest request) {
        // Step 1: Validate name field is not empty
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Full Name is required.");
        }
        // Step 2: Validate name format (only letters and spaces allowed)
        if (!request.getName().trim().matches("^[a-zA-Z\\s]+$")) {
            throw new BadRequestException("Full Name should only contain alphabetic characters and spaces (no numbers or special characters).");
        }
        // Step 3: Validate email format (must contain @ and .)
        if (request.getEmail() == null || !request.getEmail().contains("@") || !request.getEmail().contains(".")) {
            throw new BadRequestException("Please enter a valid email address.");
        }
        // Step 4: Validate password length (min 6 characters)
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BadRequestException("Password must be at least 6 characters long.");
        }
        // Step 5: Check if email is already registered in database
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("An account with this email address already exists!");
        }

        // Step 6: Create new User entity and populate properties
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        // Hash the plain-text password securely before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("PATIENT"); // Default role is PATIENT
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        user.setCity(request.getCity());
        user.setState(request.getState());
        user.setPincode(request.getPincode());

        // Step 7: Save user entity to database
        User savedUser = userRepository.save(user);

        // Step 8: Automatically create and link an empty Cart for the new patient
        Cart cart = new Cart();
        cart.setPatient(savedUser);
        cartRepository.save(cart);

        // Step 9: Generate a JWT session token for the user
        String token = tokenProvider.generateTokenForUser(savedUser.getId(), savedUser.getEmail());
        
        // Return success response with token and user profile details
        return new AuthResponse(token, savedUser.getId(), savedUser.getName(), savedUser.getEmail(), savedUser.getRole());
    }

    public AuthResponse login(AuthRequest request) {
        // Step 1: Validate login inputs are provided
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email address is required.");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new BadRequestException("Password is required.");
        }

        // Step 2: Retrieve user by email from repository. Throw error if not found.
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password. Please check your credentials."));

        // Step 3: Match password hash. Also supports plaintext check fallback for initial seeds.
        boolean isMatch = passwordEncoder.matches(request.getPassword(), user.getPassword()) 
                       || request.getPassword().equals(user.getPassword());

        if (!isMatch) {
            throw new BadRequestException("Invalid email or password. Please check your credentials.");
        }

        // Step 4: Generate a JWT session token upon successful login
        String token = tokenProvider.generateTokenForUser(user.getId(), user.getEmail());
        
        // Return success response with token and user details
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}

