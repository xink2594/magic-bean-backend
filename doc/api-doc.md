### 0. 通用基础规范

**统一返回结构 (JSON):**

```json
{
  "code": 200,          // 状态码：200成功，其他失败
  "msg": "success",     // 提示信息
  "data": {}            // 具体业务数据，可为 Object 或 Array
}

```

**环境变量配置提示:**
在 `application.properties` 中预留以下配置项：

```properties
cloudflare.r2.endpoint: https://<你的账户Account_ID>.r2.cloudflarestorage.com
cloudflare.r2.access-key: 你的访问密钥_Access_Key_ID
cloudflare.r2.secret-key: 你的机密访问密钥_Secret_Access_Key
cloudflare.r2.bucket-name: magic-bean
# R2 兼容 S3，但没有传统意义的地域，填 auto 即可。如果某些旧版 SDK 报错，可换成 us-east-1
cloudflare.r2.region: auto 
# 用来拼接返回给前端的图片 URL 的基础域名
#cloudflare.r2.domain: https://你选择的域名

# DashScope
alibaba.ai.api-key: 大模型 API Key
alibaba.ai.vision-model: 大模型视觉分析的模型名称（如 qwen-vl-plus）
alibaba.ai.default-plant-prompt: 默认的植物诊断提示词（System Prompt）
```

---

### 1. 图像代理模块 (Image Proxy API)

这个接口供 ESP32 或 App 调用，后端收到文件后转发给 Cloudflare 图床，隐藏真实的 Token。

**1.1 上传图片**

* **接口路径:** `POST /api/image/upload`
* **Content-Type:** `multipart/form-data`
* **请求参数:**
* `file` (File, 必填): 图片文件，建议限制大小 (如 5MB) 和格式 (jpg/png)。


* **后端处理逻辑 (喂给 Cursor 的 Prompt):**
  接收 MultipartFile，使用 `RestTemplate` 或 `WebClient` 构建 POST 请求，将文件转发至 `cloudflare.imgbed.url`，并在 Header 中带上鉴权 Token。解析 Cloudflare 返回的 JSON，提取真实图片 URL 返回给前端。
* **成功响应 (data 部分):**

```json
{
  "url": "https://pub-xxxx.r2.dev/plant_20260311_123.jpg"
}

```

---

### 2. AI 诊断模块 (AI Diagnosis API)

App 端传入图片 URL 和可选的提示词，后端组装大模型格式的 Payload 并发起请求。

**2.1 获取植物诊断建议**

* **接口路径:** `POST /api/ai/analyze`
* **Content-Type:** `application/json`
* **请求体 (Request Body):**

```json
{
  "imageUrl": "https://pub-xxxx.r2.dev/plant_20260311_123.jpg", // 必填，图床直链
  "prompt": "我的植物叶子发黄了，请帮我看看是什么问题？"        // 选填，用户的手动提问
}

```

* **后端处理逻辑 (喂给 Cursor 的 Prompt):**
  如果没有传入 `prompt`，后端需注入默认的 System Prompt（例如：“你是一个植物病理学专家，请分析图片中植物的健康状态，并给出养护建议”）。严格按照 OpenAI Vision 接口的 JSON 格式组装请求，通过 HTTP 调用大模型，提取返回的文本内容。
* **成功响应 (data 部分):**

```json
{
  "analysisResult": "从图片来看，叶片边缘发黄干枯，可能是由于近期缺水或空气湿度过低导致。建议：1. 立即浇透水... "
}

```

---

### 3. 云端数据同步模块 (Data Sync API) - MySQL 备份

配合 MyBatis-Plus，用于接收客户端 SQLite 的增量或全量数据推送，以及恢复拉取。

**预设数据库表结构 (PlantRecord):**

* `id` (主键, 自增/雪花算法):`bigint`
* `device_id` (设备标识符):`varchar(128)`
* `timestamp` (数据产生的时间戳):`bigint`
* `temperature` (温度):`double`
* `humidity` (湿度):`double`
* `image_url` (图床直链，可为空):`varchar(1024)`
* `note` (用户手记，可为空):`text`

**3.1 上行同步 (Push Data)**

* **接口路径:** `POST /api/sync/push`
* **Content-Type:** `application/json`
* **请求体 (Request Body):**

```json
{
  "deviceId": "ESP32-MAC-123456",
  "records": [
    {
      "timestamp": 1710134400000,
      "temperature": 24.5,
      "humidity": 45.2,
      "imageUrl": "https://pub-xxxx.r2.dev/pic1.jpg",
      "note": "今天叶子长得不错"
    },
    // ... 可以批量包含多条数据
  ]
}

```

* **后端处理逻辑 (喂给 Cursor 的 Prompt):**
  利用 MyBatis-Plus 的 `saveOrUpdateBatch` 方法，根据 `device_id` 和 `timestamp` 决定是插入新记录还是更新已有记录。
* **成功响应 (data 部分):**

```json
{
  "syncedCount": 10 // 成功同步的条数
}

```

**3.2 下行恢复 (Pull Data)**

* **接口路径:** `GET /api/sync/pull`
* **请求参数 (Query Params):**
* `deviceId` (String, 必填): 设备标识符。
* `lastSyncTime` (Long, 选填): 上次同步的时间戳。如果不传，则拉取该设备的所有历史数据；如果传了，则只拉取大于该时间戳的数据（增量拉取）。


* **后端处理逻辑 (喂给 Cursor 的 Prompt):**
  使用 MyBatis-Plus 的 `QueryWrapper`，按 `deviceId` 查询，根据 `lastSyncTime` 进行过滤，并按 `timestamp` 升序排序返回。
* **成功响应 (data 部分):**

```json
{
  "records": [
    {
      "id": 1001,
      "timestamp": 1710134400000,
      "temperature": 24.5,
      "humidity": 45.2,
      "imageUrl": "https://pub-xxxx.r2.dev/pic1.jpg",
      "note": "今天叶子长得不错"
    }
  ]
}

```

---