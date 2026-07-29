package com.mahmoudramadan.studentregistration.auth.dto;

public record AuthResponse(
        String accessToken,
        long expiresInSeconds,
        String refreshToken
) {}
