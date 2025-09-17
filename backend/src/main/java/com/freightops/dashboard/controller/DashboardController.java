package com.freightops.dashboard.controller;

import com.freightops.dashboard.service.DashboardService;
import com.freightops.dashboard.dto.DashboardStatsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/recent-activities")
    public ResponseEntity<?> getRecentActivities() {
        // TODO: Implement recent activities endpoint
        return ResponseEntity.ok("Recent activities endpoint - to be implemented");
    }

    @GetMapping("/revenue-chart")
    public ResponseEntity<?> getRevenueChart(@RequestParam(defaultValue = "30") int days) {
        // TODO: Implement revenue chart endpoint
        return ResponseEntity.ok("Revenue chart endpoint - to be implemented");
    }
}
