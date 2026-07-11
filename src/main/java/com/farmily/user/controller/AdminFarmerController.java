package com.farmily.user.controller;

/*
   本 class 為當初與 Vue 前端串接用的 REST API（/api/admin/farmers）
   管理後台已改用 controller/view 下的 AdminFarmerViewController（Thymeleaf）
   前端與模板皆不再呼叫這些端點

import com.farmily.user.dto.FarmerProfileResponse;
import com.farmily.user.service.AdminFarmerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/farmers")
public class AdminFarmerController {

    private final AdminFarmerService adminFarmerService;

    public AdminFarmerController(AdminFarmerService adminFarmerService) {
        this.adminFarmerService = adminFarmerService;
    }

    // 查所有小農
    @GetMapping
    public ResponseEntity<List<FarmerProfileResponse>> list() {
        return ResponseEntity.ok(adminFarmerService.listAll());
    }

    // 查單一小農
    @GetMapping("/{farmerId}")
    public ResponseEntity<FarmerProfileResponse> getOne(@PathVariable Integer farmerId) {
        return ResponseEntity.ok(adminFarmerService.getById(farmerId));
    }

    // 停權
    @PutMapping("/{farmerId}/suspend")
    public ResponseEntity<FarmerProfileResponse> suspend(@PathVariable Integer farmerId) {
        return ResponseEntity.ok(adminFarmerService.suspend(farmerId));
    }

    // 恢復
    @PutMapping("/{farmerId}/reinstate")
    public ResponseEntity<FarmerProfileResponse> reinstate(@PathVariable Integer farmerId) {
        return ResponseEntity.ok(adminFarmerService.reinstate(farmerId));
    }
}
 */