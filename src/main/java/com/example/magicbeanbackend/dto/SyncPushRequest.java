package com.example.magicbeanbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SyncPushRequest(
        @NotBlank(message = "deviceId 不能为空") @Size(max = 128, message = "deviceId 长度不能超过 128") String deviceId,
        @NotEmpty(message = "records 不能为空") @Valid List<SyncPushRecord> records) {
}
