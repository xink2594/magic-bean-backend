package com.example.magicbeanbackend.service;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;

@Service
public class VisionAnalysisService {

    @Value("${alibaba.ai.api-key}")
    private String apiKey;

    @Value("${alibaba.ai.vision-model}")
    private String modelName;

    @Value("${alibaba.ai.default-plant-prompt}")
    private String defaultPrompt;

    public String analyzeImage(String imageUrl, String userPrompt) throws Exception {
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
        return (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
    }
}
