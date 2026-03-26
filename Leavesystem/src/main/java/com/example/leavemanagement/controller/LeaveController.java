package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.ApplyLeaveRequest;
import com.example.leavemanagement.dto.LeaveResponse;
import com.example.leavemanagement.service.LeaveService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/apply/{employeeId}")
    @ResponseStatus(HttpStatus.CREATED)
    public LeaveResponse applyLeave(@PathVariable Long employeeId, @Valid @RequestBody ApplyLeaveRequest request) {
        return leaveService.applyLeave(employeeId, request);
    }

    @GetMapping("/history/{employeeId}")
    public List<LeaveResponse> getLeaveHistory(@PathVariable Long employeeId) {
        return leaveService.getLeaveHistory(employeeId);
    }

    @GetMapping
    public List<LeaveResponse> getAllLeaves() {
        return leaveService.getAllLeaves();
    }

    @DeleteMapping("/{leaveId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelLeave(@PathVariable Long leaveId) {
        leaveService.cancelLeave(leaveId);
    }
}
