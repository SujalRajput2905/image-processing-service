package com.sujalrajput.imageprocessing.controller;

import com.sujalrajput.imageprocessing.domain.User;
import com.sujalrajput.imageprocessing.dto.LoginRequest;
import com.sujalrajput.imageprocessing.dto.LoginResponse;
import com.sujalrajput.imageprocessing.dto.RegisterRequest;
import com.sujalrajput.imageprocessing.dto.RegisterResponse;
import com.sujalrajput.imageprocessing.repository.UserRepository;
import com.sujalrajput.imageprocessing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        User user = userService.register(registerRequest);

        RegisterResponse response = new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest);

        LoginResponse response = new LoginResponse(user.getUsername());

        return ResponseEntity.ok(response);
    }
}

