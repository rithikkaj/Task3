package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.LeaveType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ApplyLeaveRequest(
        @NotNull LeaveType type,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
