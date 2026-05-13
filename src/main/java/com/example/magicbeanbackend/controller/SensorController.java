package com.example.magicbeanbackend.controller;

import com.example.magicbeanbackend.common.ApiResponse;
import com.example.magicbeanbackend.dto.HistoryDataResponse;
import com.example.magicbeanbackend.dto.LatestDataResponse;
import com.example.magicbeanbackend.service.SensorService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 传感器数据控制器
 * 提供设备状态和历史传感器数据查询接口
 */
@Validated
@RestController
@RequestMapping("/api/data")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    /**
     * 获取设备最新状态与实时数据 (Device Shadow)
     * App 进入控制面板时调用，实现数据的秒开
     *
     * @param deviceId 设备的 MAC 地址
     * @return 设备最新状态响应
     */
    @GetMapping("/latest/{deviceId}")
    public ResponseEntity<ApiResponse<LatestDataResponse>> getLatestData(
            @PathVariable String deviceId) {
        LatestDataResponse response = sensorService.getLatestData(deviceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 拉取历史传感器数据 (图表数据源)
     * 用于 App 绘制环境温湿度趋势折线图
     * 若不传 startTime 和 endTime，默认查询用户时区的"今日"数据
     *
     * @param deviceId  设备 ID
     * @param startTime 查询起始时间戳 (毫秒)，可选
     * @param endTime   查询结束时间戳 (毫秒)，可选
     * @param timezone  用户所在时区 (如 "Asia/Shanghai")，可选，默认为系统时区
     * @return 历史数据响应
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<HistoryDataResponse>> getHistoryData(
            @RequestParam String deviceId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false, defaultValue = "Asia/Shanghai") String timezone) {
        HistoryDataResponse response = sensorService.getHistoryData(deviceId, startTime, endTime, timezone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
