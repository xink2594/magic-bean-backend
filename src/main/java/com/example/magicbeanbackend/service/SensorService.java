package com.example.magicbeanbackend.service;

import com.example.magicbeanbackend.dto.HistoryDataResponse;
import com.example.magicbeanbackend.dto.LatestDataResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 传感器数据服务
 * 处理设备状态和历史传感器数据查询
 */
@Service
public class SensorService {

    private final JdbcTemplate jdbcTemplate;

    public SensorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取设备最新状态与实时数据 (Device Shadow)
     * 1. 查询 plant_status 表获取设备在线状态
     * 2. 查询 plant_data 表获取最新传感器数据
     *
     * @param deviceId 设备 MAC 地址
     * @return 设备最新状态响应
     */
    public LatestDataResponse getLatestData(String deviceId) {
        // 查询设备在线状态
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM plant_status WHERE device_id = ? ORDER BY create_time DESC LIMIT 1",
                String.class,
                deviceId
        );

        // 查询最新传感器数据
        LatestDataResponse.SensorData latestData = jdbcTemplate.queryForObject(
                "SELECT timestamp, temperature, air_humidity, dirt_humidity FROM plant_data WHERE device_id = ? ORDER BY timestamp DESC LIMIT 1",
                (rs, rowNum) -> mapSensorData(rs),
                deviceId
        );

        return new LatestDataResponse(deviceId, status != null ? status : "offline", latestData);
    }

    /**
     * 拉取历史传感器数据 (图表数据源)
     * 默认仅查询今日数据（使用 create_time 字段判断）
     *
     * @param deviceId  设备 ID
     * @param startTime 查询起始时间戳 (秒级)，可选
     * @param endTime   查询结束时间戳 (秒级)，可选
     * @return 历史数据响应
     */
    public HistoryDataResponse getHistoryData(String deviceId, Long startTime, Long endTime) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, timestamp, temperature, air_humidity, dirt_humidity FROM plant_data WHERE device_id = ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(deviceId);

        // 如果未指定时间范围，默认查询今日数据
        if (startTime == null && endTime == null) {
            // 使用 create_time 字段查询今日数据
            sql.append(" AND DATE(create_time) = CURDATE()");
        } else {
            if (startTime != null) {
                sql.append(" AND timestamp >= ?");
                params.add(startTime);
            }
            if (endTime != null) {
                sql.append(" AND timestamp <= ?");
                params.add(endTime);
            }
        }

        sql.append(" ORDER BY timestamp ASC");

        List<HistoryDataResponse.SensorRecord> records = jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> mapSensorRecord(rs),
                params.toArray()
        );

        return new HistoryDataResponse(records);
    }

    /**
     * 映射传感器数据对象
     */
    private LatestDataResponse.SensorData mapSensorData(ResultSet rs) throws SQLException {
        return new LatestDataResponse.SensorData(
                rs.getLong("timestamp"),
                rs.getObject("temperature", Double.class),
                rs.getObject("air_humidity", Double.class),
                rs.getObject("dirt_humidity", Double.class)
        );
    }

    /**
     * 映射传感器记录对象
     */
    private HistoryDataResponse.SensorRecord mapSensorRecord(ResultSet rs) throws SQLException {
        return new HistoryDataResponse.SensorRecord(
                rs.getLong("id"),
                rs.getLong("timestamp"),
                rs.getObject("temperature", Double.class),
                rs.getObject("air_humidity", Double.class),
                rs.getObject("dirt_humidity", Double.class)
        );
    }
}
