package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.ApplyLeaveRequest;
import com.example.leavemanagement.dto.LeaveResponse;
import com.example.leavemanagement.dto.UpdateLeaveStatusRequest;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveStatus;
import com.example.leavemanagement.entity.Role;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.exception.ResourceNotFoundException;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeaveService {

    private static final Set<LeaveStatus> ACTIVE_STATUSES = Set.of(LeaveStatus.PENDING, LeaveStatus.APPROVED);

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeService employeeService;

    public LeaveService(LeaveRequestRepository leaveRequestRepository, EmployeeService employeeService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeService = employeeService;
    }

    public LeaveResponse applyLeave(Long employeeId, ApplyLeaveRequest request) {
        validateLeaveDates(request.startDate(), request.endDate());
        Employee employee = employeeService.getEmployeeEntity(employeeId);

        boolean hasOverlap = leaveRequestRepository
                .existsByAppliedByIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        employeeId,
                        ACTIVE_STATUSES,
                        request.endDate(),
                        request.startDate()
                );
        if (hasOverlap) {
            throw new BadRequestException("Leave dates overlap with an existing active leave request.");
        }

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .type(request.type())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(LeaveStatus.PENDING)
                .appliedBy(employee)
                .build();

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaveHistory(Long employeeId) {
        employeeService.getEmployeeEntity(employeeId);
        return leaveRequestRepository.findByAppliedByIdOrderByStartDateDesc(employeeId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaves() {
        return leaveRequestRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void cancelLeave(Long leaveId) {
        LeaveRequest leaveRequest = getLeaveEntity(leaveId);
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be canceled.");
        }
        leaveRequestRepository.delete(leaveRequest);
    }

    public LeaveResponse updateLeaveStatus(Long leaveId, UpdateLeaveStatusRequest request) {
        if (request.status() == LeaveStatus.PENDING) {
            throw new BadRequestException("Leave status can only be updated to APPROVED or REJECTED.");
        }

        LeaveRequest leaveRequest = getLeaveEntity(leaveId);
        Employee admin = employeeService.getEmployeeEntity(request.adminId());

        if (admin.getRole() != Role.ADMIN) {
            throw new BadRequestException("Only admins can approve or reject leave requests.");
        }
        if (leaveRequest.getAppliedBy().getId().equals(admin.getId())) {
            throw new BadRequestException("Admin cannot approve or reject their own leave request.");
        }
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be updated.");
        }

        leaveRequest.setStatus(request.status());
        leaveRequest.setApprovedBy(admin);
        leaveRequest.setApprovedAt(OffsetDateTime.now());

        return mapToResponse(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeaves() {
        return getLeavesByStatus(LeaveStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeavesByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatusOrderByStartDateAsc(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeavesApprovedBy(Long adminId) {
        Employee admin = employeeService.getEmployeeEntity(adminId);
        if (admin.getRole() != Role.ADMIN) {
            throw new BadRequestException("Employee " + adminId + " is not an admin.");
        }
        return leaveRequestRepository.findByApprovedByIdOrderByApprovedAtDesc(adminId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private LeaveRequest getLeaveEntity(Long leaveId) {
        return leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request " + leaveId + " not found."));
    }

    private void validateLeaveDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be on or after start date.");
        }
    }

    private LeaveResponse mapToResponse(LeaveRequest leaveRequest) {
        return new LeaveResponse(
                leaveRequest.getId(),
                leaveRequest.getType(),
                leaveRequest.getStartDate(),
                leaveRequest.getEndDate(),
                leaveRequest.getStatus(),
                leaveRequest.getAppliedBy().getId(),
                leaveRequest.getAppliedBy().getName(),
                leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getId() : null,
                leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getName() : null,
                leaveRequest.getApprovedAt()
        );
    }
}
