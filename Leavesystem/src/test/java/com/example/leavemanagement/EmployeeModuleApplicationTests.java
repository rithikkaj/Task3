package com.example.leavemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.leavemanagement.dto.CreateEmployeeRequest;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EmployeeModuleApplicationTests {

    @Autowired
    private EmployeeService employeeService;

    @Test
    void employeeCanBeCreatedAndReadBack() {
        var created = employeeService.createEmployee(
                new CreateEmployeeRequest("Jane Doe", "jane@example.com", Role.EMPLOYEE)
        );

        var fetched = employeeService.getEmployeeById(created.id());

        assertThat(fetched.name()).isEqualTo("Jane Doe");
        assertThat(fetched.email()).isEqualTo("jane@example.com");
        assertThat(fetched.role()).isEqualTo(Role.EMPLOYEE);
    }

    @Test
    void duplicateEmailIsRejected() {
        employeeService.createEmployee(new CreateEmployeeRequest("Admin", "admin@example.com", Role.ADMIN));

        assertThatThrownBy(() -> employeeService.createEmployee(
                new CreateEmployeeRequest("Another Admin", "admin@example.com", Role.ADMIN)
        )).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }
}
