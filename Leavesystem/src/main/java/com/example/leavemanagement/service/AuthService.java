package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.AuthRequest;
import com.example.leavemanagement.dto.AuthResponse;
import com.example.leavemanagement.dto.CreateEmployeeRequest;
import com.example.leavemanagement.dto.RegisterRequest;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.repository.EmployeeRepository;
import com.example.leavemanagement.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final EmployeeService employeeService;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            EmployeeRepository employeeRepository,
            EmployeeService employeeService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.employeeRepository = employeeRepository;
        this.employeeService = employeeService;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        employeeRepository.findByEmailIgnoreCase(request.email())
                .ifPresent(employee -> {
                    throw new BadRequestException("Employee with email " + request.email() + " already exists.");
                });

        employeeService.createEmployee(
                new CreateEmployeeRequest(request.name(), request.email(), request.password(), request.role())
        );

        return login(new AuthRequest(request.email(), request.password()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Employee employee = employeeRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("Employee with email " + request.email() + " not found."));
        var user = org.springframework.security.core.userdetails.User.builder()
                .username(employee.getEmail())
                .password(employee.getPassword())
                .roles(employee.getRole().name())
                .build();

        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                "Bearer",
                employee.getId(),
                employee.getEmail(),
                employee.getRole().name()
        );
    }
}
