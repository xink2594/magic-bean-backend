package com.example.magicbeanbackend.controller;

import com.example.magicbeanbackend.dto.PlantAnalysisRequest;
import com.example.magicbeanbackend.dto.PlantAnalysisResponse;
import com.example.magicbeanbackend.service.VisionAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class VisionController {

    private final VisionAnalysisService visionAnalysisService;

    public VisionController(VisionAnalysisService visionAnalysisService) {
        this.visionAnalysisService = visionAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<PlantAnalysisResponse> analyzePlant(
            @RequestBody PlantAnalysisRequest request) {
        try {
            // 参数校验
            if (request.imageUrl() == null || request.imageUrl().isEmpty()) {
                throw new IllegalArgumentException("imageUrl 不能为空");
            }

            // 调用服务
            String resultText = visionAnalysisService.analyzeImage(request.imageUrl(), request.prompt());

            // 封装返回结果
            return ResponseEntity.ok(new PlantAnalysisResponse(resultText));

        } catch (Exception e) {
            // 生产环境中建议使用全局异常处理器 (@ControllerAdvice) 处理异常
            throw new RuntimeException("大模型调用失败: " + e.getMessage(), e);
        }
    }
}