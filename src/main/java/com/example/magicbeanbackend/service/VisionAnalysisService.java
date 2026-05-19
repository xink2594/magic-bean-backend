package com.example.magicbeanbackend.service;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.example.magicbeanbackend.dto.PlantAnalysisResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class VisionAnalysisService {

    @Value("${alibaba.ai.api-key}")
    private String apiKey;

    @Value("${alibaba.ai.vision-model}")
    private String modelName;

    @Value("${alibaba.ai.default-plant-prompt}")
    private String defaultPrompt;

    /**
     * 分析植物图片并返回结构化结果
     *
     * @param imageUrl  图片 URL
     * @param userPrompt 用户自定义提示词（可选）
     * @return 结构化的分析结果
     */
    public PlantAnalysisResponse analyzeImage(String imageUrl, String userPrompt) throws Exception {
        // 1. 决定最终的 Prompt：如果用户未传入，则使用配置文件中的默认提示词
        String finalPrompt = StringUtils.hasText(userPrompt) ? userPrompt : defaultPrompt;

        // 2. 初始化多模态对话对象
        MultiModalConversation conv = new MultiModalConversation();

        // 3. 构建包含图片和文本的消息体
        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Arrays.asList(
                        Collections.singletonMap("image", imageUrl),
                        Collections.singletonMap("text", finalPrompt)
                )).build();

        // 4. 构建请求参数
        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(modelName)
                .messages(Arrays.asList(userMessage))
                .build();

        // 5. 调用大模型并获取结果
        MultiModalConversationResult result = conv.call(param);

        // 6. 从返回的复杂 JSON 结构中提取最终的文本内容
        String rawText = (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");

        // 7. 解析结构化数据并返回
        return parseAnalysisResult(rawText);
    }

    /**
     * 解析 AI 返回的文本，提取结构化数据
     * 格式：
     * 植物品种：[...]
     * 长势分析：[...]
     * 培养建议：[...]
     */
    private PlantAnalysisResponse parseAnalysisResult(String rawText) {
        String plantVariety = extractField(rawText, "植物品种[：:]\\s*(.+?)(?=\\n|$)");
        String growthAnalysis = extractField(rawText, "长势分析[：:]\\s*(.+?)(?=\\n|$)");
        String cultivationAdvice = extractField(rawText, "培养建议[：:]\\s*(.+?)(?=\\n|$)");
        String lightAdvice = extractField(rawText, "补光建议[：:]\\s*(.+?)(?=\\n|$)");
        String waterAdvice = extractField(rawText, "补水建议[：:]\\s*(.+?)(?=\\n|$)");

        // 如果解析失败，返回原始文本作为长势分析
        if (plantVariety == null && growthAnalysis == null && cultivationAdvice == null) {
            return new PlantAnalysisResponse("未知", rawText, "暂无", "暂无", "暂无");
        }

        return new PlantAnalysisResponse(
                plantVariety != null ? plantVariety : "未知",
                growthAnalysis != null ? growthAnalysis : "暂无",
                cultivationAdvice != null ? cultivationAdvice : "暂无",
                lightAdvice != null ? lightAdvice : "暂无",
                waterAdvice != null ? waterAdvice : "暂无"
        );
    }

    /**
     * 使用正则表达式提取指定字段的内容
     */
    private String extractField(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
