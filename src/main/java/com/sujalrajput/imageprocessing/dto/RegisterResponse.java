package com.sujalrajput.imageprocessing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class RegisterResponse {
    private long id;
    private String username;

    private Instant createdAt;
}
