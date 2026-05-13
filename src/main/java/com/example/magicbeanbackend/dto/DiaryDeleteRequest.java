package com.example.magicbeanbackend.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 删除/恢复手记请求 DTO
 * 用于 POST /api/diary/delete 和 /api/diary/restore 接口
 */
public record DiaryDeleteRequest(
        @NotNull(message = "id 不能为空")
        Long id
) {
}
