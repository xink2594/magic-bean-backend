package com.example.magicbeanbackend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 保存/编辑手记请求 DTO
 * 用于 POST /api/diary/save 接口
 */
public record DiarySaveRequest(
        @NotNull(message = "id 不能为空")
        Long id,

        String note
) {
}
