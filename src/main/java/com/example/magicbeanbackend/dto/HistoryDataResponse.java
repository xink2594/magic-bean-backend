package com.example.magicbeanbackend.dto;

import java.util.List;

/**
 * 历史传感器数据响应 DTO
 * 用于 GET /api/data/history 接口
 */
public record HistoryDataResponse(
        List<SensorRecord> records
) {
    /**
     * 传感器记录子对象
     */
    public record SensorRecord(
            Long id,
            Long timestamp,
            Double temperature,
            Double airHumidity,
            Double dirtHumidity
    ) {
    }
}
