package com.example.magicbeanbackend.service;

import com.example.magicbeanbackend.dto.PlantRecordDto;
import com.example.magicbeanbackend.dto.SyncPushRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SyncService {

    private final JdbcTemplate jdbcTemplate;

    public SyncService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int push(String deviceId, List<SyncPushRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            return 0;
        }

        int syncedCount = 0;
        for (SyncPushRecord record : records) {
            int updated = jdbcTemplate.update(
                    "UPDATE plant_record SET temperature = ?, humidity = ?, image_url = ?, note = ? WHERE device_id = ? AND `timestamp` = ?",
                    record.temperature(),
                    record.humidity(),
                    record.imageUrl(),
                    record.note(),
                    deviceId,
                    record.timestamp());

            if (updated == 0) {
                jdbcTemplate.update(
                        "INSERT INTO plant_record (device_id, `timestamp`, temperature, humidity, image_url, note) VALUES (?, ?, ?, ?, ?, ?)",
                        deviceId,
                        record.timestamp(),
                        record.temperature(),
                        record.humidity(),
                        record.imageUrl(),
                        record.note());
            }

            syncedCount++;
        }
        return syncedCount;
    }

    public List<PlantRecordDto> pull(String deviceId, Long lastSyncTime) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, `timestamp`, temperature, humidity, image_url, note FROM plant_record WHERE device_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(deviceId);

        if (lastSyncTime != null) {
            sql.append(" AND `timestamp` > ?");
            params.add(lastSyncTime);
        }
        sql.append(" ORDER BY `timestamp` ASC");

        return jdbcTemplate.query(sql.toString(), this::mapRecord, params.toArray());
    }

    private PlantRecordDto mapRecord(ResultSet rs, int rowNum) throws SQLException {
        return new PlantRecordDto(
                rs.getLong("id"),
                rs.getLong("timestamp"),
                rs.getObject("temperature", Double.class),
                rs.getObject("humidity", Double.class),
                rs.getString("image_url"),
                rs.getString("note"));
    }
}
