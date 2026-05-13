package com.example.magicbeanbackend.dto;

import java.util.List;

/**
 * 手记画廊列表响应 DTO
 * 用于 GET /api/diary/list 接口
 */
public record DiaryListResponse(
        List<DiaryRecord> records
) {
    /**
     * 日记记录子对象
     */
    public record DiaryRecord(
            Long id,
            Long timestamp,
            String imageUrl,
            Double temperature,
            Double airHumidity,
            Double dirtHumidity,
            String note
    ) {
    }
}
