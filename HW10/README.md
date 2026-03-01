---

# 应用系统体系架构 — 作业10

> n8n Agent 工作流 + MCP Tool 调用 + 前端集成

---

# 📌 一、系统目标

本次作业实现：

* 基于 n8n 构建 AI Agent 工作流
* 接入 OpenAI 模型
* 引入 Simple Memory 实现多轮对话
* 集成 MCP Tool 访问书籍数据库
* 输出 JSON 格式响应
* 前端集成 Agent 服务

---

# 📌 二、整体架构

```text
前端聊天窗口
        ↓
Webhook / Chat Trigger
        ↓
AI Agent
   ├── OpenAI Chat Model
   ├── Simple Memory
   └── MCP Client
        ↓
Respond to Webhook
        ↓
前端展示结果
```

---

# 📌 三、核心节点配置

---

## 1️⃣ Chat Trigger（聊天触发器）

模式：**Embedded Chat**

作用：

* 支持传入会话 ID
* 实现多轮对话
* 接收用户文本输入

预设格式要求：

> 必须使用 Markdown 格式列出书籍清单
> 且对书名和价格进行加粗

---

## 2️⃣ AI Agent 节点

AI Agent 是整个流程的核心调度节点。

输入来源：

* Chat Trigger 的用户消息

挂载子节点：

* OpenAI Chat Model
* Simple Memory
* MCP Client

---

## 3️⃣ OpenAI Chat Model

模型：

```
gpt-5-mini
```

作用：

* 生成自然语言回答
* 按指定 Markdown 格式输出

---

## 4️⃣ Simple Memory

Context Window Length：

```
30
```

说明：

* 保留最近 30 次交互
* 实现上下文连续性
* 支持多轮对话

例如：

用户第一轮：

> 推荐 Python 书籍

第二轮：

> 再推荐几本入门的

模型可以理解“入门的”是指 Python 入门。

---

## 5️⃣ MCP Client

挂载的 MCP 工具包括：

* `get_all_books`
* `search_books_by_title`

作用：

* 访问真实数据库
* 获取书籍数据
* 避免模型幻觉

调用端口：

```
8001
```

---

## 6️⃣ Respond to Webhook

作用：

* 将 AI 处理结果封装为 JSON
* 返回前端聊天窗口

---

# 📌 四、完整执行流程

---

## 用户输入

例如：

```
有什么关于 Python 的书？
```

---

## 流程执行步骤

1. Chat Trigger 捕获用户文本
2. AI Agent 接管任务
3. 从 Simple Memory 读取历史对话
4. 判断是否需要调用 MCP Tool
5. 若涉及书籍数据 → 调用 MCP Client
6. 获取工具返回结果
7. 组合：

   * 提示词
   * 上下文
   * 工具结果
8. 发送给 OpenAI Chat Model
9. 模型生成 Markdown 格式回答
10. Respond to Webhook 返回 JSON

---

# 📌 五、MCP Tool 调用示例

---

## 1️⃣ get_all_books

场景：

用户提问：

```
有哪些书？
```

流程：

* Agent 调用 MCP
* 获取全部图书
* 返回 Markdown 列表

---

## 2️⃣ search_books_by_title

场景：

```
推荐 Python 入门书
```

流程：

* Agent 调用 MCP 模糊查询
* 返回匹配书籍
* 生成格式化响应

---

# 📌 六、输出格式示例

```json
{
  "reply": "以下是关于 **Python** 的推荐书籍：\n\n- **Python编程入门** - **¥89**\n- **Python核心技术** - **¥99**"
}
```

满足要求：

* Markdown 格式
* 书名加粗
* 价格加粗

---

# 📌 七、前端集成

前端调用方式：

```javascript
fetch("http://localhost:5678/webhook/chat", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    message: "推荐 Python 书籍",
    sessionId: "user-001"
  })
})
```

返回结果：

* JSON 格式
* 渲染为 Markdown

---

# 📌 八、Agent 设计亮点

---

## 1️⃣ 多轮对话

依赖：

* Simple Memory

---

## 2️⃣ 工具调用能力

通过 MCP：

* 访问真实数据库
* 减少幻觉
* 保证数据真实性

---

## 3️⃣ 模型与工具协作

模型负责：

* 自然语言理解
* 输出格式

工具负责：

* 数据查询
* 真实信息

属于：

> LLM + Tool 的协作架构

