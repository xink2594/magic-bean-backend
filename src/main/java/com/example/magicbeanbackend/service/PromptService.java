package com.example.magicbeanbackend.service;

import com.example.magicbeanbackend.dto.PromptResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备提示词服务
 * 处理 plant_prompt 表的查询与 upsert
 */
@Service
public class PromptService {

    private final JdbcTemplate jdbcTemplate;

    public PromptService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 获取设备的自定义提示词
     *
     * @param deviceId 设备 ID
     * @return 提示词响应，不存在则返回 null
     */
    public PromptResponse getPrompt(String deviceId) {
        List<PromptResponse> results = jdbcTemplate.query(
                "SELECT device_id, prompt FROM plant_prompt WHERE device_id = ?",
                (rs, rowNum) -> new PromptResponse(
                        rs.getString("device_id"),
                        rs.getString("prompt")
                ),
                deviceId
        );
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 新增或更新设备的自定义提示词
     * device_id 已存在则更新，不存在则插入
     *
     * @param deviceId 设备 ID
     * @param prompt   提示词内容
     * @return 操作是否成功
     */
    @Transactional
    public boolean upsertPrompt(String deviceId, String prompt) {
        int affected = jdbcTemplate.update(
                "INSERT INTO plant_prompt (device_id, prompt) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE prompt = VALUES(prompt)",
                deviceId,
                prompt
        );
        // INSERT 返回 1，UPDATE（无变化）返回 0，UPDATE（有变化）返回 2
        return affected >= 0;
    }
}
