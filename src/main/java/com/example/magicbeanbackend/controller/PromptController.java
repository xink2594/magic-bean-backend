package com.example.magicbeanbackend.controller;

import com.example.magicbeanbackend.common.ApiResponse;
import com.example.magicbeanbackend.dto.PromptRequest;
import com.example.magicbeanbackend.dto.PromptResponse;
import com.example.magicbeanbackend.service.PromptService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 设备提示词控制器
 * 提供设备自定义 AI 提示词的查询与设置接口
 */
@Validated
@RestController
@RequestMapping("/api/prompt")
public class PromptController {

    private final PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    /**
     * 获取设备的自定义提示词
     *
     * @param deviceId 设备 ID
     * @return 提示词响应
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PromptResponse>> getPrompt(@RequestParam String deviceId) {
        PromptResponse response = promptService.getPrompt(deviceId);
        if (response != null) {
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            return ResponseEntity.ok(ApiResponse.fail(404, "未找到该设备的提示词"));
        }
    }

    /**
     * 新增或更新设备的自定义提示词
     *
     * @param request 包含 deviceId 和 prompt 的请求体
     * @return 操作结果
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> savePrompt(@Valid @RequestBody PromptRequest request) {
        boolean success = promptService.upsertPrompt(request.deviceId(), request.prompt());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success(null));
        } else {
            return ResponseEntity.ok(ApiResponse.fail(500, "保存提示词失败"));
        }
    }
}
