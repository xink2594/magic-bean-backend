package com.example.magicbeanbackend.dto;

import java.util.List;

/**
 * 手记画廊列表响应 DTO
 * 用于 GET /api/diary/list 接口（只返回 id 和 imageUrl）
 */
public record DiaryListResponse(
        List<DiaryRecord> records
) {
    /**
     * 日记记录子对象（精简版）
     */
    public record DiaryRecord(
            Long id,
            String imageUrl
    ) {
    }
}
