package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.LeaveStatus;
import com.example.leavemanagement.entity.LeaveType;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record LeaveResponse(
        Long id,
        LeaveType type,
        LocalDate startDate,
        LocalDate endDate,
        LeaveStatus status,
        Long employeeId,
        String employeeName,
        Long approvedById,
        String approvedByName,
        OffsetDateTime approvedAt
) {
}
