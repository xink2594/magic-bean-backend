------

# Magic Bean (魔法豆) 后端 API 文档与架构规范

## 0. 全局架构与数据库设计

### 0.1 核心数据流转规范 (MQTT + HTTP 混合架构)

系统的实时通信与高频数据流完全剥离 HTTP，采用 MQTT 协议与 EMQX 规则引擎处理：

1. **高频数据上报**: 环境温湿度、土壤湿度 -> `ESP32` -> 发布至 `plant/{mac}/data` -> `EMQX 规则引擎` -> 自动插入 MySQL `plant_data` 表。
2. **上下线状态监控**: `ESP32` 启动/断开 -> 发布至 `plant/{mac}/status` (带 LWT 遗嘱) -> `EMQX 规则引擎` -> 自动插入/更新 MySQL `plant_status` 表。
3. **远程命令与画廊联动**: `App` -> 发布指令至 `plant/{mac}/cmd` -> `ESP32` 拍照并获取图床 URL -> 回传至 `plant/{mac}/response` -> `EMQX 规则引擎` -> 自动插入 MySQL `plant_diary` 表。

### 0.2 数据库表结构 (MySQL)

基于 `magic_bean.sql` 初始化的核心表结构如下：

**表1：`plant_data` (传感器历史数据)**

用于记录设备上报的温湿度环境信息。

- `id` (bigint, 主键, 自增)
- `device_id` (varchar(64)): 设备 MAC 地址或唯一标识
- `timestamp` (bigint): 整点/半点的时间戳 (毫秒)
- `temperature` (double): 环境温度
- `air_humidity` (double): 空气湿度
- `dirt_humidity` (double): 土壤湿度
- `create_time` (datetime): 云端入库时间

**表2：`plant_diary` (植物生长日记与画廊)**

用于记录设备自动拍摄的照片或用户的手动笔记。

- `id` (bigint, 主键, 自增)
- `device_id` (varchar(64)): 设备标识
- `timestamp` (bigint): 手记创建的时间戳(毫秒)
- `temperature`, `air_humidity`, `dirt_humidity` (double): 拍照/记录瞬间的环境数据
- `image_url` (varchar(1024)): 图床直链
- `note` (text): 用户手记内容
- `create_time`, `update_time`, `is_deleted` (基础审计字段)

**表3：`plant_status` (设备在线状态表)**

用于记录设备的在线/离线状态。

- `id` (int, 主键, 自增)
- `device_id` (varchar(64)): 设备标识
- `status` (varchar(64)): 状态值 (online / offline)
- `create_time` (datetime): 云端入库/更新时间

### 0.3 通用返回结构 (JSON)

JSON

```json
{
  "code": 200,          // 状态码：200成功，其他失败
  "msg": "success",     // 提示信息
  "data": {}            // 具体业务数据
}
```

------

## 1. 图像与 AI 诊断模块 (HTTP API)

### 1.1 上传图片至 R2 图床

- **接口路径:** `POST /api/image/upload`
- **Content-Type:** `multipart/form-data`
- **请求参数:**
  - `file` (File, 必填): 图片文件，建议限制大小 (如 5MB) 和格式 (jpg/png)。
- **成功响应 (data):**

JSON

```json
{
  "url": "https://pub-xxxx.r2.dev/plant_20260421_123.jpg"
}
```

### 1.2 获取大模型植物诊断建议

- **接口路径:** `POST /api/ai/analyze`
- **Content-Type:** `application/json`
- **请求体:**

JSON

```json
{
  "imageUrl": "https://pub-xxxx.r2.dev/plant_20260421_123.jpg", // 必填，图床直链
  "prompt": "我的植物叶子发黄了，请帮我看看是什么问题？"        			// 选填，用户的手动提问
}
```

- **成功响应 (data):**

JSON

```json
{
  "analysisResult": "从图片来看，叶片边缘发黄干枯，可能是缺水导致。建议立即浇透水..."
}
```

------

## 2. 传感器数据与状态模块 (HTTP API)

此模块用于前端 App 获取设备的实时状态（设备影子）以及绘制折线图所需的历史数据。

### 2.1 获取设备最新状态与实时数据 (Device Shadow)

App 进入控制面板时调用，避免等待 MQTT 异步消息，实现数据的秒开。

- **接口路径:** `GET /api/data/latest/{deviceId}`
- **路径参数:** - `deviceId` (String, 必填): 设备的 MAC 地址
- **后端处理逻辑:**
  1. 查询 `plant_status` 表，按 `device_id` 获取最新一条记录的 `status`。
  2. 查询 `plant_data` 表，按 `device_id` 获取 `timestamp` 最大的一条传感器记录。
- **成功响应 (data):**

JSON

```json
{
  "deviceId": "A8610A102C9F",
  "status": "online",
  "latestData": {
    "timestamp": 1713686400000,
    "temperature": 26.0,
    "airHumidity": 48.0,
    "dirtHumidity": 35.5
  }
}
```

### 2.2 拉取历史传感器数据 (图表数据源)

用于 App 绘制环境温湿度趋势折线图。

- **数据库逻辑：** 仅调用当天的数据绘制图像

- **接口路径:** `GET /api/data/history`
- **请求参数 (Query Params):**
  - `deviceId` (String, 必填)
  - `startTime` (Long, 选填): 查询起始时间戳
  - `endTime` (Long, 选填): 查询结束时间戳
- **成功响应 (data):**

JSON

```json
{
  "records": [
    {
      "id": 123,
      "timestamp": 1713686400000,
      "temperature": 26.0,
      "airHumidity": 48.0,
      "dirtHumidity": 35.5
    }
  ]
}
```

------

## 3. 植物手记与画廊模块 (HTTP API)

演示阶段采用“云端为准”模式。App 直接读写后端的 `plant_diary` 表，实现画廊的渲染与文字编辑。

### 3.1 获取手记画廊列表

用于 App 的“画廊”或“时间轴”页面，展示历史照片及其伴随的环境指标。

- **接口路径:** `GET /api/diary/list`

- **请求参数 (Query Params):**

  - `deviceId` (String, 必填)
  - `limit` (Int, 选填): 返回记录条数，默认 20。

- **后端逻辑:**

  查询 `plant_diary` 表，按 `timestamp` **倒序排列**（最新照片排最前），过滤掉 `is_deleted = 1` 的记录。

- **成功响应 (data):**

JSON

```json
{
  "records": [
    {
      "id": 1,
      "timestamp": 1713686400000,
      "imageUrl": "https://pub-xxxx.r2.dev/plant_20260421.jpg",
      "temperature": 26.0,
      "airHumidity": 48.0,
      "dirtHumidity": 35.5,
      "note": "今天叶片看起来非常有精神"
    }
  ]
}
```

### 3.2 保存/编辑手记内容

用户在 App 画廊中点击某张由设备自动拍下的照片，为其补充文字心得。

- **接口路径:** `POST /api/diary/save`
- **Content-Type:** `application/json`
- **请求体:**

JSON

```json
{
  "id": 1,              // 必填，对应数据库 plant_diary 表的记录主键
  "note": "重新调整了摆放位置，光照更充足了。" // 覆盖或新增的文字内容
}
```

- **后端逻辑:**

  根据 `id` 主键更新 `plant_diary` 表中的 `note` 字段。

- **成功响应:**

JSON

```json
{
  "code": 200,
  "msg": "success"
}
```
