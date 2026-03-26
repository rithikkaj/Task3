package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.Role;

public record EmployeeResponse(
        Long id,
        String name,
        String email,
        Role role
) {
}
