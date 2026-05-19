package com.example.magicbeanbackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 设备提示词请求 DTO
 * 用于 POST /api/prompt 接口
 */
public record PromptRequest(
        @NotBlank(message = "deviceId 不能为空")
        String deviceId,

        @NotBlank(message = "prompt 不能为空")
        String prompt
) {
}
