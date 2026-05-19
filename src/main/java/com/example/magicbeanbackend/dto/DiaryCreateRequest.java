package com.example.magicbeanbackend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 新增手记请求 DTO
 * 用于 POST /api/diary/create 接口
 * 温湿度数据由后端自动查询 plant_data 补全
 */
public record DiaryCreateRequest(
        @NotBlank(message = "deviceId 不能为空")
        String deviceId,

        @NotBlank(message = "imageUrl 不能为空")
        String imageUrl,

        String note
) {
}
