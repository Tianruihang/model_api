# 日常问答API接口文档

## 概述
这是一个日常问答的RESTful API接口，提供日常问题的增删改查功能。

## 基础信息
- 基础URL: `http://localhost:9885/api/daily-questions`
- 请求方式: REST风格
- 数据格式: JSON
- 字符编码: UTF-8

## 数据模型

### DailyQuestionModel
```json
{
  "id": "主键ID",
  "question": "问题内容",
  "answer": "答案内容", 
  "category": "问题分类",
  "createTime": "创建时间",
  "updateTime": "更新时间",
  "status": "状态(1-启用,0-禁用)",
  "sortWeight": "排序权重",
  "remark": "备注"
}
```

## API接口列表

### 1. 添加日常问答
- **接口地址**: `POST /api/daily-questions/add`
- **请求参数**: 
```json
{
  "question": "问题内容",
  "answer": "答案内容",
  "category": "问题分类",
  "status": 1,
  "sortWeight": 0,
  "remark": "备注"
}
```
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "添加成功"
}
```

### 2. 更新日常问答
- **接口地址**: `PUT /api/daily-questions/update`
- **请求参数**: 
```json
{
  "id": 1,
  "question": "更新后的问题",
  "answer": "更新后的答案",
  "category": "更新后的分类"
}
```
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "更新成功"
}
```

### 3. 删除日常问答
- **接口地址**: `DELETE /api/daily-questions/delete/{id}`
- **路径参数**: id - 问答ID
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "删除成功"
}
```

### 4. 根据ID获取日常问答
- **接口地址**: `GET /api/daily-questions/get/{id}`
- **路径参数**: id - 问答ID
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "查询成功"
}
```

### 5. 获取所有启用的日常问答
- **接口地址**: `GET /api/daily-questions/list`
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "查询成功，共31条记录"
}
```

### 6. 根据分类获取日常问答
- **接口地址**: `GET /api/daily-questions/category/{category}`
- **路径参数**: category - 问题分类
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "查询成功，共8条记录"
}
```

### 7. 搜索日常问答
- **接口地址**: `GET /api/daily-questions/search?keyword={keyword}`
- **查询参数**: keyword - 搜索关键词
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "搜索成功，共5条记录"
}
```

### 8. 分页获取日常问答
- **接口地址**: `GET /api/daily-questions/page?page={page}&size={size}`
- **查询参数**: 
  - page: 页码(默认1)
  - size: 每页大小(默认10)
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "分页查询成功"
}
```

### 9. 获取随机日常问答
- **接口地址**: `GET /api/daily-questions/random`
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "获取随机问答成功"
}
```

### 10. 获取统计信息
- **接口地址**: `GET /api/daily-questions/statistics`
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "统计信息获取成功"
}
```

### 11. 语义匹配问答
- **接口地址**: `POST /api/daily-questions/semantic-match`
- **请求参数**: 
```json
{
  "question": "用户问题",
  "threshold": 0.6,
  "maxResults": 1
}
```
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "语义匹配成功",
  "data": {
    "dailyQuestion": {
      "id": 1,
      "question": "最近过得怎么样？",
      "answer": "您好！我是中国银行数字人...",
      "category": "问候"
    },
    "similarity": 0.85,
    "matched": true,
    "matchReason": "问题比较相似"
  }
}
```

### 12. 语义匹配多个问答
- **接口地址**: `POST /api/daily-questions/semantic-match-multiple`
- **请求参数**: 
```json
{
  "question": "用户问题",
  "threshold": 0.6,
  "maxResults": 3
}
```
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "语义匹配成功，找到3个结果",
  "data": [
    {
      "dailyQuestion": {...},
      "similarity": 0.85,
      "matched": true,
      "matchReason": "问题比较相似"
    }
  ]
}
```

### 13. 计算问题相似度
- **接口地址**: `POST /api/daily-questions/calculate-similarity`
- **请求参数**: 
  - question1: 问题1
  - question2: 问题2
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "相似度计算成功",
  "data": {
    "question1": "最近怎么样？",
    "question2": "最近过得怎么样？",
    "similarity": 0.85
  }
}
```

### 14. 简化语义匹配 - 直接返回答案
- **接口地址**: `POST /api/daily-questions/simple-match`
- **请求参数**: 
```json
{
  "question": "用户问题",
  "threshold": 0.6
}
```
- **响应示例**:
```json
{
  "code": 200,
  "resultStr": "语义匹配成功",
  "data": {
    "answer": "您好！我是中国银行数字人，最近工作很充实，正在为更多客户提供优质的金融服务。希望您也一切顺利！",
    "similarity": 0.85,
    "matched": true,
    "matchReason": "问题比较相似"
  }
}
```

## 问题分类说明
系统预设了以下问题分类：
- 问候 - 日常问候类问题
- 天气 - 天气相关话题
- 生活 - 生活安排、日常生活类问题
- 健康 - 健康生活类问题
- 交通 - 交通出行类问题
- 工作 - 工作相关类问题
- 学习 - 学习成长类问题
- 美食 - 美食推荐、美食相关类问题
- 娱乐 - 娱乐休闲类问题
- 旅行 - 旅行度假类问题
- 家庭 - 家庭生活类问题
- 社交 - 社交活动类问题
- 时事 - 时事新闻类问题
- 目标 - 目标规划类问题
- 系统 - 系统介绍类问题

## 状态码说明
- 200: 成功
- 400: 请求参数错误
- 404: 资源不存在
- 500: 服务器内部错误

## 使用示例

### 添加新问答
```bash
curl -X POST http://localhost:9885/api/daily-questions/add \
  -H "Content-Type: application/json" \
  -d '{
    "question": "今天心情怎么样？",
    "answer": "今天心情很好，天气晴朗，工作顺利！",
    "category": "生活",
    "remark": "心情相关问答"
  }'
```

### 搜索问答
```bash
curl "http://localhost:9885/api/daily-questions/search?keyword=天气"
```

### 分页查询
```bash
curl "http://localhost:9885/api/daily-questions/page?page=1&size=5"
```

### 语义匹配问答
```bash
curl -X POST http://localhost:9885/api/daily-questions/semantic-match \
  -H "Content-Type: application/json" \
  -d '{
    "question": "最近怎么样？",
    "threshold": 0.7
  }'
```

### 语义匹配多个问答
```bash
curl -X POST http://localhost:9885/api/daily-questions/semantic-match-multiple \
  -H "Content-Type: application/json" \
  -d '{
    "question": "今天天气如何？",
    "threshold": 0.6,
    "maxResults": 3
  }'
```

### 计算问题相似度
```bash
curl -X POST "http://localhost:9885/api/daily-questions/calculate-similarity?question1=最近怎么样&question2=最近过得怎么样？"
```

### 简化语义匹配 - 直接返回答案
```bash
curl -X POST http://localhost:9885/api/daily-questions/simple-match \
  -H "Content-Type: application/json" \
  -d '{
    "question": "最近怎么样？",
    "threshold": 0.7
  }'
```

## 语义匹配接口说明

### **接口选择建议**
- **`/semantic-match`**: 返回完整的匹配信息，包含问题、答案、分类等
- **`/simple-match`**: 简化接口，主要返回答案内容，适合直接获取回答的场景
- **`/semantic-match-multiple`**: 返回多个匹配结果，适合需要展示多个选项的场景

### **推荐使用场景**
- **聊天机器人**: 使用 `/simple-match` 接口，直接获取答案
- **智能客服**: 使用 `/semantic-match` 接口，获取完整信息
- **搜索建议**: 使用 `/semantic-match-multiple` 接口，提供多个选项

## 语义匹配算法说明

系统采用多种算法进行语义匹配：

### **相似度计算方法**
1. **Jaccard相似度** (权重40%): 基于词汇集合的相似度计算
2. **关键词匹配相似度** (权重40%): 基于关键词重叠的相似度计算  
3. **编辑距离相似度** (权重20%): 基于字符串编辑距离的相似度计算

### **匹配阈值设置**
- 默认阈值: 0.6 (60%)
- 可自定义阈值范围: 0.0 - 1.0
- 相似度越高，匹配越精确

### **匹配结果说明**
- 相似度 ≥ 0.9: 问题高度相似
- 相似度 ≥ 0.8: 问题非常相似
- 相似度 ≥ 0.7: 问题比较相似
- 相似度 ≥ 0.6: 问题有一定相似性
- 相似度 < 0.6: 问题相似度较低

## 注意事项
1. 所有时间字段使用ISO 8601格式
2. 问题内容最大长度500字符
3. 答案内容支持长文本
4. 分类字段为可选，支持自定义分类
5. 状态字段：1表示启用，0表示禁用
6. 排序权重越大，排序越靠前
7. 系统会自动设置创建时间和更新时间
8. 语义匹配支持中文问题，自动进行文本标准化处理

## 数据库配置
确保在`application-dev.yml`中配置了正确的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://your-database-host:3306/your-database-name
    username: your-username
    password: your-password
```

## 部署说明
1. 执行`src/main/resources/sql/daily_question.sql`创建数据库表
2. 启动Spring Boot应用
3. 访问`http://localhost:9885/api/daily-questions/list`测试接口
