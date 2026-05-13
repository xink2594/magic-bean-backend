package com.example.magicbeanbackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 用于验证后端服务是否正常运行
 */
@RestController
public class HealthController {

    /**
     * 根路径健康检查
     * 访问 http://192.168.123.160:8080 即可看到响应
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "✅ 你可以成功连接到后端！");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("service", "Magic Bean Backend");
        return ResponseEntity.ok(response);
    }
}
