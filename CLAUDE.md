# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Magic Bean (魔法豆) — 一个 IoT 植物养护平台的 Spring Boot 后端。ESP32 设备通过 MQTT 上报传感器数据，本后端通过 HTTP API 为前端 App 提供数据查询、图片上传、AI 植物诊断等服务。

## 构建与运行

```bash
# 构建
./mvnw clean package

# 运行（默认激活 local profile）
./mvnw spring-boot:run

# 运行 JAR
java -jar target/magic-bean-backend-0.0.1-SNAPSHOT.jar

# 测试
./mvnw test
```

应用运行在端口 8080。配置文件使用 `application.properties` + `application-local.properties`（本地凭证，已提交到 git 需注意）。

## 架构

三层架构：Controller → Service → Database，无 ORM，无接口抽象。

### 关键技术决策

- **框架**: Spring Boot 4.0.3 (WebMVC + JDBC + Validation)，Java 21
- **数据库访问**: 使用 Spring `JdbcTemplate` + 手写 SQL，无 JPA/MyBatis。Service 层直接内联 SQL 字符串。
- **DTO**: 全部使用 Java 21 `record` 类型，不可变值对象。
- **统一响应**: `ApiResponse<T>(code, msg, data)` record，`success()` / `fail()` 静态工厂方法。
- **异常处理**: `GlobalExceptionHandler` 使用 `@RestControllerAdvice` 统一捕获。
- **CORS**: `CorsConfig` 注册全局 Filter。
- **请求日志**: `RequestLoggingFilter` 记录 HTTP 请求的路径、方法、状态码、耗时。
- **图片存储**: AWS S3 SDK 对接 Cloudflare R2，仅支持 jpg/jpeg/png，最大 5MB。
- **AI 视觉**: 阿里云 DashScope SDK（多模态模型），通过 `alibaba.ai.vision-model` 配置项指定模型名，AI 输出为纯文本结构化结果（非 JSON），由 `VisionAnalysisService` 用正则解析。
- **自定义提示词**: 支持 per-device 自定义 AI 提示词，存储在 `plant_prompt` 表中。

### 目录结构

```
com.example.magicbeanbackend/
├── common/          # 跨切面：ApiResponse, CorsConfig, GlobalExceptionHandler, RequestLoggingFilter
├── controller/      # REST 端点，仅做参数校验和转发
│   ├── HealthController.java      # 健康检查
│   ├── SensorController.java      # 传感器数据查询
│   ├── DiaryController.java       # 手记 CRUD + 回收站
│   ├── ImageController.java       # 图片上传
│   ├── VisionController.java      # AI 植物诊断
│   └── PromptController.java      # 设备自定义提示词
├── dto/             # 请求/响应 record 定义
└── service/         # 业务逻辑 + SQL 查询
    ├── SensorService.java         # 设备状态 + 传感器数据
    ├── DiaryService.java          # 手记 CRUD
    ├── ImageUploadService.java    # R2 图片上传
    ├── VisionAnalysisService.java # DashScope AI 诊断 + 文本解析
    └── PromptService.java         # 设备自定义提示词 CRUD
```

### 数据库 (MySQL magic_bean)

四张表，DDL 在 `/magic_bean.sql`（注意：`plant_prompt` 表 DDL 未包含在 SQL 文件中）：
- `plant_data` — 传感器历史数据 (device_id, timestamp, temperature, air_humidity, dirt_humidity)
- `plant_diary` — 植物手记/画廊 (含 image_url, note, 软删除 is_deleted)
- `plant_status` — 设备在线状态
- `plant_prompt` — 设备自定义 AI 提示词 (device_id, prompt)

数据写入由 EMQX 规则引擎完成（MQTT → MySQL），后端只负责读取和 diary/prompt 的增删改。

### API 端点

详细文档见 `doc/api.md`。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/` | 健康检查 |
| GET | `/api/data/latest/{deviceId}` | 设备影子（最新状态+传感器） |
| GET | `/api/data/history` | 历史传感器数据（图表用） |
| GET | `/api/diary/list` | 手记画廊列表（分页） |
| GET | `/api/diary/detail` | 手记详情 |
| POST | `/api/diary/create` | 新增手记（自动补全温湿度） |
| POST | `/api/diary/save` | 编辑手记 |
| POST | `/api/diary/delete` | 软删除手记 |
| POST | `/api/diary/restore` | 恢复已删除手记 |
| GET | `/api/diary/trash` | 回收站列表 |
| POST | `/api/image/upload` | 上传图片到 R2 |
| POST | `/api/ai/analyze` | AI 植物健康诊断 |
| GET | `/api/prompt` | 获取设备自定义提示词 |
| POST | `/api/prompt` | 新增或更新设备自定义提示词 |

## 代码约定

- **无 Lombok** — 手写 getter/setter 或直接用 record。
- **无接口层** — Service 是具体类，无 Service Interface。
- **SQL 写在 Service 中** — 无 Repository/DAO 层。
- **配置注入** — 使用 `@Value` 从 properties 读取。
- **验证** — 使用 Jakarta Bean Validation (`@NotNull`, `@Valid`)。
- **AI 提示词** — 存在 `alibaba.ai.default-plant-prompt` 配置项中，输出格式为纯文本结构化结果（非 JSON），详见 `doc/prompt.md`。
- **构造器注入** — Controller/Service 使用构造器注入依赖（无 `@Autowired` 字段注入）。

## 文档

- `doc/api.md` — 完整 API 文档与数据库设计
- `doc/prompt.md` — AI 植物诊断的系统提示词
- `doc/spring_ai_alibaba_doc.md` — Spring AI Alibaba 参考文档
- `doc/spring_ai_alibaba_example.md` — 示例代码
