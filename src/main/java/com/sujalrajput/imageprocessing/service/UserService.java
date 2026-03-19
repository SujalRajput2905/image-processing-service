package com.sujalrajput.imageprocessing.service;

import com.sujalrajput.imageprocessing.config.security.JwtUtil;
import com.sujalrajput.imageprocessing.domain.User;
import com.sujalrajput.imageprocessing.dto.LoginRequest;
import com.sujalrajput.imageprocessing.dto.RegisterRequest;
import com.sujalrajput.imageprocessing.exception.AuthenticationException;
import com.sujalrajput.imageprocessing.exception.UserAlreadyExistsException;
import com.sujalrajput.imageprocessing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public User register(RegisterRequest registerRequest) {

        Optional<User> existingUser = userRepository
                .findByUsername(registerRequest.getUsername());

        if(existingUser.isPresent()) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        String encodedPassword = passwordEncoder
                .encode(registerRequest.getPassword());

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encodedPassword);

        return userRepository.save(user);
    }

    public String login(LoginRequest loginRequest) {
        Optional<User> currUser = userRepository
                .findByUsername(loginRequest.getUsername());

        if(currUser.isEmpty()) {
            throw new AuthenticationException("Invalid username or password");
        }

        User user = currUser.get();
        boolean matches = passwordEncoder.matches(
                loginRequest.getPassword(), user.getPassword()
        );

        if(!matches) {
            throw new AuthenticationException("Invalid username or password");
        }

        return jwtUtil.generateToken(user.getUsername());
    }
}
