package com.automeds.controller;

import com.automeds.dto.UpdateProfileRequest;
import com.automeds.dto.UserProfileDTO;
import com.automeds.entity.User;
import com.automeds.exception.ResourceNotFoundException;
import com.automeds.repository.UserRepository;
import com.automeds.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
        return ResponseEntity.ok(mapToDTO(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateProfileRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        user.setName(request.getName().trim());
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);
        user.setCity(request.getCity() != null ? request.getCity().trim() : null);
        user.setState(request.getState() != null ? request.getState().trim() : null);
        user.setPincode(request.getPincode() != null ? request.getPincode().trim() : null);
        user.setAllergies(request.getAllergies() != null ? request.getAllergies().trim() : null);
        user.setChronicConditions(request.getChronicConditions() != null ? request.getChronicConditions().trim() : null);

        User updated = userRepository.save(user);
        return ResponseEntity.ok(mapToDTO(updated));
    }

    private UserProfileDTO mapToDTO(User user) {
        return new UserProfileDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getAddress(),
                user.getCity(),
                user.getState(),
                user.getPincode(),
                user.getAllergies(),
                user.getChronicConditions(),
                user.getCreatedAt()
        );
    }
}
