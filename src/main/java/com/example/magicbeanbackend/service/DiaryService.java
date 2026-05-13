package com.example.magicbeanbackend.service;

import com.example.magicbeanbackend.dto.DiaryDetailResponse;
import com.example.magicbeanbackend.dto.DiaryListResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 植物手记服务
 * 处理植物日记与画廊相关业务逻辑
 */
@Service
public class DiaryService {

    private final JdbcTemplate jdbcTemplate;

    public DiaryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取手记画廊列表（精简版，只返回 id 和 imageUrl）
     * 按 timestamp 倒序排列，过滤掉已删除的记录
     *
     * @param deviceId 设备 ID
     * @param limit    返回记录条数，默认 20
     * @return 手记列表响应
     */
    public DiaryListResponse getDiaryList(String deviceId, int limit) {
        List<DiaryListResponse.DiaryRecord> records = jdbcTemplate.query(
                "SELECT id, image_url FROM plant_diary " +
                        "WHERE device_id = ? AND (is_deleted IS NULL OR is_deleted = 0) " +
                        "ORDER BY timestamp DESC " +
                        "LIMIT ?",
                (rs, rowNum) -> new DiaryListResponse.DiaryRecord(
                        rs.getLong("id"),
                        rs.getString("image_url")
                ),
                deviceId,
                limit
        );

        return new DiaryListResponse(records);
    }

    /**
     * 获取手记详情
     *
     * @param deviceId 设备 ID
     * @param id       记录主键 ID
     * @return 手记详情，不存在则返回 null
     */
    public DiaryDetailResponse getDiaryDetail(String deviceId, Long id) {
        List<DiaryDetailResponse> results = jdbcTemplate.query(
                "SELECT id, timestamp, image_url, temperature, air_humidity, dirt_humidity, note " +
                        "FROM plant_diary " +
                        "WHERE device_id = ? AND id = ? AND (is_deleted IS NULL OR is_deleted = 0)",
                this::mapDiaryDetail,
                deviceId,
                id
        );

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 保存/编辑手记内容
     * 根据 id 主键更新 plant_diary 表中的 note 字段
     *
     * @param id   记录主键 ID
     * @param note 手记文字内容
     * @return 更新是否成功
     */
    @Transactional
    public boolean saveDiary(Long id, String note) {
        int updated = jdbcTemplate.update(
                "UPDATE plant_diary SET note = ?, update_time = NOW() WHERE id = ?",
                note,
                id
        );
        return updated > 0;
    }

    /**
     * 映射日记详情对象
     */
    private DiaryDetailResponse mapDiaryDetail(ResultSet rs, int rowNum) throws SQLException {
        return new DiaryDetailResponse(
                rs.getLong("id"),
                rs.getLong("timestamp"),
                rs.getString("image_url"),
                rs.getObject("temperature", Double.class),
                rs.getObject("air_humidity", Double.class),
                rs.getObject("dirt_humidity", Double.class),
                rs.getString("note")
        );
    }
}
