package com.example.magicbeanbackend.dto;

/**
 * 手记详情响应 DTO
 * 用于 GET /api/diary/detail 接口
 */
public record DiaryDetailResponse(
        Long id,
        Long timestamp,
        String imageUrl,
        Double temperature,
        Double airHumidity,
        Double dirtHumidity,
        String note
) {
}
