package com.example.magicbeanbackend.controller;

import com.example.magicbeanbackend.common.ApiResponse;
import com.example.magicbeanbackend.dto.PlantAnalysisRequest;
import com.example.magicbeanbackend.dto.PlantAnalysisResponse;
import com.example.magicbeanbackend.service.VisionAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 视觉分析控制器
 * 提供植物图片分析接口
 */
@RestController
@RequestMapping("/api/ai")
public class VisionController {

    private final VisionAnalysisService visionAnalysisService;

    public VisionController(VisionAnalysisService visionAnalysisService) {
        this.visionAnalysisService = visionAnalysisService;
    }

    /**
     * 分析植物图片
     *
     * @param request 包含 imageUrl 和可选的 prompt
     * @return 结构化的分析结果
     */
    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<PlantAnalysisResponse>> analyzePlant(
            @RequestBody PlantAnalysisRequest request) {
        // 参数校验
        if (request.imageUrl() == null || request.imageUrl().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.fail(400, "imageUrl 不能为空"));
        }

        try {
            // 调用服务获取结构化结果
            PlantAnalysisResponse response = visionAnalysisService.analyzeImage(request.imageUrl(), request.prompt());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail(500, "大模型调用失败: " + e.getMessage()));
        }
    }
}