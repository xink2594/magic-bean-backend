package com.example.magicbeanbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SyncPushRecord(
        @NotNull(message = "timestamp 不能为空") Long timestamp,
        Double temperature,
        Double humidity,
        @Size(max = 1024, message = "imageUrl 长度不能超过 1024") String imageUrl,
        String note) {
}
