# Magic Bean (魔法豆) Backend

IoT 植物养护平台后端。ESP32 设备通过 MQTT 上报传感器数据，后端通过 HTTP API 为前端 App 提供数据查询、图片上传、AI 植物诊断等服务。

## 技术栈

| 组件 | 技术 |
|------|------|
| 框架 | Spring Boot 4.0.3 (WebMVC + JDBC + Validation) |
| 语言 | Java 21 |
| 数据库 | MySQL 8.0 (JdbcTemplate, 无 ORM) |
| 图片存储 | Cloudflare R2 (AWS S3 SDK) |
| AI 视觉 | 阿里云 DashScope SDK |
| 消息协议 | MQTT (EMQX 规则引擎 → MySQL) |
| 构建工具 | Maven |

## 快速开始

### 前置条件

- Java 21
- MySQL 8.0+
- Cloudflare R2 账号
- 阿里云 DashScope API Key

### 配置

编辑 `src/main/resources/application-local.properties`，填入真实凭证：

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/magic_bean
spring.datasource.username=root
spring.datasource.password=your_password

# Cloudflare R2
cloudflare.r2.endpoint=https://{Account_ID}.r2.cloudflarestorage.com
cloudflare.r2.access-key={Access_Key_ID}
cloudflare.r2.secret-key={Secret_Access_Key}
cloudflare.r2.domain=https://pub-xxxx.r2.dev

# DashScope
alibaba.ai.api-key={DASHSCOPE_API_KEY}
alibaba.ai.vision-model={model-name}
```

### 构建与运行

```bash
# 构建
./mvnw clean package

# 运行
./mvnw spring-boot:run

# 或运行 JAR
java -jar target/magic-bean-backend-0.0.1-SNAPSHOT.jar

# 测试
./mvnw test
```

应用运行在 `http://localhost:8080`。

## 架构

```
ESP32 → MQTT → EMQX 规则引擎 → MySQL (plant_data, plant_status)
App   → HTTP  → Spring Boot    → MySQL (plant_diary, plant_prompt)
                                     → Cloudflare R2 (图片)
                                     → DashScope (AI 诊断)
```

三层架构：Controller → Service → Database。无 ORM、无 JPA/MyBatis，Service 层直接写 SQL。DTO 使用 Java 21 `record` 类型。

## API

统一响应格式：

```json
{
  "code": 200,
  "msg": "success",
  "data": {}
}
```

主要端点：

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/data/latest/{deviceId}` | 设备影子（最新状态+传感器） |
| GET | `/api/data/history` | 历史传感器数据 |
| GET | `/api/diary/list` | 手记画廊列表 |
| POST | `/api/diary/create` | 新增手记 |
| POST | `/api/image/upload` | 上传图片到 R2 |
| POST | `/api/ai/analyze` | AI 植物健康诊断 |
| GET/POST | `/api/prompt` | 设备自定义提示词 |

完整 API 文档见 [doc/api.md](doc/api.md)。

## 项目结构

```
com.example.magicbeanbackend/
├── common/          # ApiResponse, CorsConfig, GlobalExceptionHandler, RequestLoggingFilter
├── controller/      # REST 端点
├── dto/             # 请求/响应 record 定义
└── service/         # 业务逻辑 + SQL 查询
```

## 数据库

MySQL 数据库 `magic_bean`，包含以下核心表：

- **plant_data** — 传感器历史数据（由 EMQX 规则引擎自动写入）
- **plant_diary** — 植物手记/画廊（支持软删除）
- **plant_status** — 设备在线状态
- **plant_prompt** — 设备自定义 AI 提示词

表结构初始化见 [magic_bean.sql](magic_bean.sql)。

## 文档

- [doc/api.md](doc/api.md) — 完整 API 文档与数据库设计
- [doc/prompt.md](doc/prompt.md) — AI 植物诊断的系统提示词

## License

[MIT](LICENSE)
