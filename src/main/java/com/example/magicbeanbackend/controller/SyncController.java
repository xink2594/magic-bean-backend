package com.example.magicbeanbackend.controller;

import com.example.magicbeanbackend.common.ApiResponse;
import com.example.magicbeanbackend.dto.SyncPullResponse;
import com.example.magicbeanbackend.dto.SyncPushRequest;
import com.example.magicbeanbackend.dto.SyncPushResponse;
import com.example.magicbeanbackend.service.SyncService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/push")
    public ResponseEntity<ApiResponse<SyncPushResponse>> push(@Valid @RequestBody SyncPushRequest request) {
        int count = syncService.push(request.deviceId(), request.records());
        return ResponseEntity.ok(ApiResponse.success(new SyncPushResponse(count)));
    }

    @GetMapping("/pull")
    public ResponseEntity<ApiResponse<SyncPullResponse>> pull(
            @RequestParam @NotBlank(message = "deviceId 不能为空") @Size(max = 128, message = "deviceId 长度不能超过 128") String deviceId,
            @RequestParam(required = false) Long lastSyncTime) {
        return ResponseEntity.ok(ApiResponse.success(new SyncPullResponse(syncService.pull(deviceId, lastSyncTime))));
    }
}
