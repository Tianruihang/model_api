# 移动数字人导航 API 接口文档

## 基础信息
- **Base URL**: `http://{host}:{port}`
- **接口前缀**: 根据 Spring Boot 配置的 `server.servlet.context-path` 确定

---

## 1. 导航到指定位置

**接口地址**: `POST /cmd/nav_name`

> 请求示例
```json
{
    "point": "A"
}
```

**请求参数**:
| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| point | string | 是 | 目标地点名称 |

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "开始导航"
}
```

---

## 2. 获取导航状态

**接口地址**: `GET /reeman/nav_status`

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "查询成功",
    "data": {
        "res": 3,
        "reason": 0,
        "goal": "A",
        "dist": 0.5,
        "mileage": 0.6
    }
}
```

**响应参数说明**:
| 名称 | 类型 | 说明 |
|------|------|------|
| res | int | 导航状态 (1=进行中, 3=到达, 6=空闲) |
| reason | int | 原因码 |
| goal | string | 目标地点 |
| dist | double | 距离目标的比例 (0.0-1.0) |
| mileage | double | 里程 |

---

## 3. 启动自定义导航循环

**接口地址**: `POST /nav/start_loop`

> 请求示例
```json
["A", "B", "C", "D", "E"]
```

**请求参数**: 字符串数组，指定要循环导航的地点列表

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "导航循环已启动"
}
```

---

## 4. 启动默认导航循环

**接口地址**: `POST /nav/start_loop_default`

**说明**: 使用默认地点 A -> B -> C -> D -> E 进行循环导航

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "默认导航循环已启动，地点: A -> B -> C -> D -> E"
}
```

---

## 5. 从配置文件启动导航循环

**接口地址**: `POST /nav/start_from_config`

**说明**: 从 `application.yml` 的 `custom` 配置中读取地点列表开始导航，支持音频路径配置

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "从配置文件第一个地点开始导航"
}
```

---

## 6. 停止导航循环

**接口地址**: `POST /nav/stop_loop`

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "导航循环已停止"
}
```

---

## 7. 获取当前导航地点

**接口地址**: `GET /nav/current`

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "查询成功",
    "data": {
        "currentDestination": "B",
        "isPaused": false
    }
}
```

**响应参数说明**:
| 名称 | 类型 | 说明 |
|------|------|------|
| currentDestination | string | 当前导航的目标地点 |
| isPaused | boolean | 是否处于暂停状态 |

**无导航时的响应**:
```json
{
    "code": 404,
    "resultStr": "未在导航中"
}
```

---

## 8. 暂停导航

**接口地址**: `POST /nav/pause`

**说明**: 暂停当前导航循环，机器人继续移动但不再检查到达状态

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "导航已暂停"
}
```

---

## 9. 继续导航

**接口地址**: `POST /nav/resume`

**说明**: 恢复暂停的导航循环，继续检查到达状态

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "导航已继续"
}
```

---

## 10. 独立导航到指定地点

**接口地址**: `POST /nav/go_to`

**说明**: 导航到指定地点，到达后自动根据配置文件中的音频路径播报音频。如果导航循环正在运行，会先暂停，独立导航完成后自动恢复。

> 请求示例
```json
{
    "destination": "C"
}
```

**请求参数**:
| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| destination | string | 是 | 目标地点名称 |

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "开始导航到：C",
    "data": {
        "destination": "C"
    }
}
```

**工作流程**:
1. 发送导航请求到机器人
2. 每1秒检查一次导航状态（延迟10秒开始检查）
3. 当到达目标地点时，自动推送对应的音频文件
4. 如果之前有导航循环在运行，自动恢复导航循环

---

## 11. 获取导航服务器配置

**接口地址**: `GET /config/nav_server`

**说明**: 获取当前导航服务器的配置，优先读取缓存值，缓存中没有则读取配置文件默认值。

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "查询成功",
    "data": {
        "host": "192.168.3.43",
        "port": 80,
        "navPath": "/cmd/nav_name",
        "statusPath": "/reeman/nav_status"
    }
}
```

**响应参数说明**:
| 名称 | 类型 | 说明 |
|------|------|------|
| host | string | 导航服务器IP地址 |
| port | int | 导航服务器端口 |
| navPath | string | 导航请求接口路径 |
| statusPath | string | 状态查询接口路径 |

---

## 12. 更新导航服务器配置

**接口地址**: `POST /config/nav_server`

**说明**: 更新导航服务器配置，配置会存入Redis缓存，下次请求优先使用缓存值。

> 请求示例
```json
{
    "host": "192.168.3.50",
    "port": 8080
}
```

**请求参数**:
| 名称 | 类型 | 必填 | 说明 |
|------|------|------|------|
| host | string | 否 | 导航服务器IP地址 |
| port | int | 否 | 导航服务器端口 |
| navPath | string | 否 | 导航请求接口路径 |
| statusPath | string | 否 | 状态查询接口路径 |

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "配置更新成功"
}
```

---

## 13. 清除导航服务器配置缓存

**接口地址**: `DELETE /config/nav_server`

**说明**: 清除Redis中的配置缓存，系统将恢复使用配置文件中的默认值。

**响应示例**:
```json
{
    "code": 200,
    "resultStr": "配置缓存已清除，将使用默认值"
}
```

---

## 配置文件格式

在 `application.yml` 中配置导航地点和音频：

```yaml
custom:
  1: wy1,5,data/wyaudio/wy1.wav
  2: wy2,5,data/wyaudio/wy2.wav
  3: wy3,5,data/wyaudio/wy3.wav
```

**格式说明**: `序号: 目的地名称,等待分钟数,音频文件路径`

---

## 通用响应格式

```json
{
    "code": 200,
    "resultStr": "操作描述",
    "data": {}
}
```

| 状态码 | 含义 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
