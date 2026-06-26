package com.paintandpetals.controller;

import com.paintandpetals.dto.request.CustomerRegisterRequest;
import com.paintandpetals.dto.request.LoginRequest;
import com.paintandpetals.dto.request.VendorRegisterRequest;
import com.paintandpetals.dto.response.AuthResponse;
import com.paintandpetals.dto.response.UserResponse;
import com.paintandpetals.mapper.DtoMapper;
import com.paintandpetals.security.SecurityUtils;
import com.paintandpetals.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SecurityUtils securityUtils;
    private final DtoMapper mapper;

    @PostMapping("/register/customer")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerCustomer(@Valid @RequestBody CustomerRegisterRequest request) {
        return authService.registerCustomer(request);
    }

    @PostMapping("/register/vendor")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerVendor(@Valid @RequestBody VendorRegisterRequest request) {
        return authService.registerVendor(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return mapper.toUserResponse(securityUtils.getCurrentUser());
    }
}
