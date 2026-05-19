package com.example.magicbeanbackend.dto;

/**
 * 植物分析响应 DTO
 * 返回结构化的 AI 分析结果
 */
public record PlantAnalysisResponse(
        String plantVariety,      // 植物品种
        String growthAnalysis,    // 长势分析
        String cultivationAdvice, // 培养建议
        String lightAdvice,       // 补光建议
        String waterAdvice        // 补水建议
) {
}
