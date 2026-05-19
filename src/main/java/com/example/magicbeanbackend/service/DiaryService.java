package com.example.magicbeanbackend.service;

import com.example.magicbeanbackend.dto.DiaryDetailResponse;
import com.example.magicbeanbackend.dto.DiaryListResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
     * 获取手记画廊列表（分页，只返回 id 和 imageUrl）
     * 按 timestamp 倒序排列，过滤掉已删除的记录
     *
     * @param deviceId 设备 ID
     * @param page     页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 手记列表响应（含分页信息）
     */
    public DiaryListResponse getDiaryList(String deviceId, int page, int pageSize) {
        // 查询总记录数
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM plant_diary WHERE device_id = ? AND (is_deleted IS NULL OR is_deleted = 0)",
                Long.class,
                deviceId
        );
        total = total != null ? total : 0L;

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / pageSize);

        // 计算偏移量
        int offset = (page - 1) * pageSize;

        // 分页查询
        List<DiaryListResponse.DiaryRecord> records = jdbcTemplate.query(
                "SELECT id, image_url FROM plant_diary " +
                        "WHERE device_id = ? AND (is_deleted IS NULL OR is_deleted = 0) " +
                        "ORDER BY timestamp DESC " +
                        "LIMIT ? OFFSET ?",
                (rs, rowNum) -> new DiaryListResponse.DiaryRecord(
                        rs.getLong("id"),
                        rs.getString("image_url")
                ),
                deviceId,
                pageSize,
                offset
        );

        return new DiaryListResponse(records, total, page, pageSize, totalPages);
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
     * 根据 id 主键更新 plant_diary 表中的 note 和传感器字段
     * 传感器字段为 null 时默认设为 0
     *
     * @param id            记录主键 ID
     * @param note          手记文字内容
     * @param temperature   温度（默认 0）
     * @param airHumidity   空气湿度（默认 0）
     * @param dirtHumidity  土壤湿度（默认 0）
     * @return 更新是否成功
     */
    @Transactional
    public boolean saveDiary(Long id, String note, Double temperature, Double airHumidity, Double dirtHumidity) {
        double temp = temperature != null ? temperature : 0;
        double air = airHumidity != null ? airHumidity : 0;
        double dirt = dirtHumidity != null ? dirtHumidity : 0;

        int updated = jdbcTemplate.update(
                "UPDATE plant_diary SET note = ?, temperature = ?, air_humidity = ?, dirt_humidity = ?, update_time = NOW() WHERE id = ?",
                note,
                temp,
                air,
                dirt,
                id
        );
        return updated > 0;
    }

    /**
     * 新增手记
     * 后端自动查询 plant_data 获取设备最新温湿度，生成当前时间戳
     *
     * @param deviceId  设备 ID
     * @param imageUrl  图片 URL
     * @param note      手记文字内容（可选）
     * @return 新增是否成功
     */
    @Transactional
    public boolean createDiary(String deviceId, String imageUrl, String note) {
        // 查询设备最新传感器数据
        Map<String, Object> sensorData = jdbcTemplate.queryForMap(
                "SELECT temperature, air_humidity, dirt_humidity FROM plant_data " +
                        "WHERE device_id = ? AND temperature != 0 AND air_humidity != 0 " +
                        "ORDER BY timestamp DESC LIMIT 1",
                deviceId
        );

        double temperature = ((Number) sensorData.get("temperature")).doubleValue();
        double airHumidity = ((Number) sensorData.get("air_humidity")).doubleValue();
        double dirtHumidity = ((Number) sensorData.get("dirt_humidity")).doubleValue();
        long timestamp = System.currentTimeMillis() / 1000;

        int inserted = jdbcTemplate.update(
                "INSERT INTO plant_diary (device_id, timestamp, temperature, air_humidity, dirt_humidity, image_url, note) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                deviceId,
                timestamp,
                temperature,
                airHumidity,
                dirtHumidity,
                imageUrl,
                note
        );
        return inserted > 0;
    }

    /**
     * 软删除手记
     * 将 is_deleted 字段设置为 1
     *
     * @param id 记录主键 ID
     * @return 删除是否成功
     */
    @Transactional
    public boolean deleteDiary(Long id) {
        int updated = jdbcTemplate.update(
                "UPDATE plant_diary SET is_deleted = 1, update_time = NOW() WHERE id = ?",
                id
        );
        return updated > 0;
    }

    /**
     * 恢复已删除的手记
     * 将 is_deleted 字段设置为 0
     *
     * @param id 记录主键 ID
     * @return 恢复是否成功
     */
    @Transactional
    public boolean restoreDiary(Long id) {
        int updated = jdbcTemplate.update(
                "UPDATE plant_diary SET is_deleted = 0, update_time = NOW() WHERE id = ?",
                id
        );
        return updated > 0;
    }

    /**
     * 获取回收站列表（已删除的记录）
     *
     * @param deviceId 设备 ID
     * @param page     页码（从 1 开始）
     * @param pageSize 每页条数
     * @return 手记列表响应（含分页信息）
     */
    public DiaryListResponse getTrashList(String deviceId, int page, int pageSize) {
        // 查询已删除的总记录数
        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM plant_diary WHERE device_id = ? AND is_deleted = 1",
                Long.class,
                deviceId
        );
        total = total != null ? total : 0L;

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / pageSize);

        // 计算偏移量
        int offset = (page - 1) * pageSize;

        // 分页查询已删除的记录
        List<DiaryListResponse.DiaryRecord> records = jdbcTemplate.query(
                "SELECT id, image_url FROM plant_diary " +
                        "WHERE device_id = ? AND is_deleted = 1 " +
                        "ORDER BY update_time DESC " +
                        "LIMIT ? OFFSET ?",
                (rs, rowNum) -> new DiaryListResponse.DiaryRecord(
                        rs.getLong("id"),
                        rs.getString("image_url")
                ),
                deviceId,
                pageSize,
                offset
        );

        return new DiaryListResponse(records, total, page, pageSize, totalPages);
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
