package com.paintandpetals.service;

import com.paintandpetals.dto.request.CustomerRegisterRequest;
import com.paintandpetals.dto.request.LoginRequest;
import com.paintandpetals.dto.request.VendorRegisterRequest;
import com.paintandpetals.dto.response.AuthResponse;
import com.paintandpetals.dto.response.UserResponse;
import com.paintandpetals.entity.User;
import com.paintandpetals.entity.VendorProfile;
import com.paintandpetals.entity.enums.Role;
import com.paintandpetals.entity.enums.VendorStatus;
import com.paintandpetals.exception.BadRequestException;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.repository.UserRepository;
import com.paintandpetals.repository.VendorProfileRepository;
import com.paintandpetals.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final DtoMapper mapper;

    @Transactional
    public AuthResponse registerCustomer(CustomerRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.CUSTOMER)
                .vendorStatus(VendorStatus.NOT_APPLICABLE)
                .build();

        userRepository.save(user);
        return authenticate(request.getEmail(), request.getPassword());
    }

    @Transactional
    public AuthResponse registerVendor(VendorRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(Role.VENDOR)
                .vendorStatus(VendorStatus.PENDING)
                .build();

        user = userRepository.save(user);

        VendorProfile profile = VendorProfile.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .description(request.getDescription())
                .build();
        vendorProfileRepository.save(profile);
        user.setVendorProfile(profile);

        return authenticate(request.getEmail(), request.getPassword());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        return authenticate(request.getEmail(), request.getPassword());
    }

    private AuthResponse authenticate(String email, String password) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        String token = jwtTokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(email).orElseThrow();
        UserResponse userResponse = mapper.toUserResponse(user);
        return AuthResponse.builder().token(token).user(userResponse).build();
    }
}
