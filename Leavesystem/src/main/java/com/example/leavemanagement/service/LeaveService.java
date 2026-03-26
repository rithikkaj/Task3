package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.ApplyLeaveRequest;
import com.example.leavemanagement.dto.LeaveResponse;
import com.example.leavemanagement.entity.Employee;
import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveStatus;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.exception.ResourceNotFoundException;
import com.example.leavemanagement.repository.LeaveRequestRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LeaveService {

    private static final Set<LeaveStatus> ACTIVE_STATUSES = Set.of(LeaveStatus.PENDING);

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
                leaveRequest.getAppliedBy().getName()
        );
    }
}
