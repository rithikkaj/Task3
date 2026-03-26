package com.example.leavemanagement.repository;

import com.example.leavemanagement.entity.LeaveRequest;
import com.example.leavemanagement.entity.LeaveStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByAppliedByIdOrderByStartDateDesc(Long employeeId);

    List<LeaveRequest> findByStatusOrderByStartDateAsc(LeaveStatus status);

    List<LeaveRequest> findByApprovedByIdOrderByApprovedAtDesc(Long adminId);

    boolean existsByAppliedByIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long employeeId,
            Collection<LeaveStatus> statuses,
            LocalDate endDate,
            LocalDate startDate
    );
}
