package com.example.magicbeanbackend.dto;

/**
 * 设备提示词响应 DTO
 * 用于 GET /api/prompt 接口
 */
public record PromptResponse(
        String deviceId,
        String prompt
) {
}
