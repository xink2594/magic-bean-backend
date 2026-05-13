package com.example.magicbeanbackend.dto;

/**
 * 设备最新状态响应 DTO
 * 用于 GET /api/data/latest/{deviceId} 接口
 */
public record LatestDataResponse(
        String deviceId,
        String status,
        SensorData latestData
) {
    /**
     * 传感器数据子对象
     */
    public record SensorData(
            Long timestamp,
            Double temperature,
            Double airHumidity,
            Double dirtHumidity
    ) {
    }
}
