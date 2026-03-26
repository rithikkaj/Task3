package com.example.leavemanagement.dto;

public record AuthResponse(
        String token,
        String tokenType,
        Long employeeId,
        String email,
        String role
) {
}
