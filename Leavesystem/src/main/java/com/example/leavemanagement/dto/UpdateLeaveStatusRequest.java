package com.example.leavemanagement.dto;

import com.example.leavemanagement.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLeaveStatusRequest(
        @NotNull Long adminId,
        @NotNull LeaveStatus status
) {
}
