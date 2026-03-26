package com.example.leavemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.leavemanagement.dto.ApplyLeaveRequest;
import com.example.leavemanagement.dto.CreateEmployeeRequest;
import com.example.leavemanagement.dto.EmployeeResponse;
import com.example.leavemanagement.dto.UpdateLeaveStatusRequest;
import com.example.leavemanagement.entity.LeaveStatus;
import com.example.leavemanagement.entity.LeaveType;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.service.EmployeeService;
import com.example.leavemanagement.service.LeaveService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LeaveManagementApplicationTests {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private LeaveService leaveService;

    @Test
    void contextLoads() {
        assertThat(employeeService).isNotNull();
        assertThat(leaveService).isNotNull();
    }

    @Test
    void adminCannotApproveOwnLeave() {
        EmployeeResponse admin = employeeService.createEmployee(
                new CreateEmployeeRequest("Admin", "admin@example.com", "password123", Role.ADMIN)
        );

        var leave = leaveService.applyLeave(
                admin.id(),
                new ApplyLeaveRequest(LeaveType.CASUAL, LocalDate.now().plusDays(1), LocalDate.now().plusDays(2))
        );

        assertThatThrownBy(() -> leaveService.updateLeaveStatus(
                leave.id(),
                new UpdateLeaveStatusRequest(admin.id(), LeaveStatus.APPROVED)
        )).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("own leave");
    }

    @Test
    void overlappingActiveLeavesAreRejected() {
        EmployeeResponse employee = employeeService.createEmployee(
                new CreateEmployeeRequest("Employee", "employee@example.com", "password123", Role.EMPLOYEE)
        );
        EmployeeResponse admin = employeeService.createEmployee(
                new CreateEmployeeRequest("Approver", "approver@example.com", "password123", Role.ADMIN)
        );

        var firstLeave = leaveService.applyLeave(
                employee.id(),
                new ApplyLeaveRequest(LeaveType.SICK, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5))
        );

        leaveService.updateLeaveStatus(
                firstLeave.id(),
                new UpdateLeaveStatusRequest(admin.id(), LeaveStatus.APPROVED)
        );

        assertThatThrownBy(() -> leaveService.applyLeave(
                employee.id(),
                new ApplyLeaveRequest(LeaveType.PAID, LocalDate.now().plusDays(4), LocalDate.now().plusDays(6))
        )).isInstanceOf(BadRequestException.class)
                .hasMessageContaining("overlap");
    }
}
