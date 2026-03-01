/**
 * 聊天服务 - 连接 n8n Agent 的流式聊天服务
 */

// n8n webhook 地址 - 请根据你的实际配置修改
const N8N_WEBHOOK_URL = 'http://localhost:5678/webhook/325060dd-e826-484c-b8c5-4f4a935e09e3/chat';

// Markdown 格式要求的系统提示词
const MARKDOWN_PROMPT = '请务必使用 Markdown 格式回答用户的问题。对于书籍列表，使用 Markdown 列表；对于书名，使用加粗（**书名**）；对于价格或关键信息，也使用加粗高亮。不要使用纯文本堆砌。\n\n用户问题：';

/**
 * 发送消息到 n8n Agent 并获取流式响应
 * @param {string} message - 用户消息
 * @param {string} sessionId - 会话ID
 * @param {function} onChunk - 接收流式数据的回调函数
 * @param {AbortSignal} signal - 用于取消请求的信号
 * @returns {Promise<void>}
 */
export async function sendMessageStream(message, sessionId, onChunk, signal) {
  try {
    const response = await fetch(N8N_WEBHOOK_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        chatInput: MARKDOWN_PROMPT + message,
        sessionId: sessionId,
      }),
      signal: signal,
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // 检查是否支持流式响应
    const contentType = response.headers.get('content-type');
    
    if (contentType && contentType.includes('text/event-stream')) {
      // SSE 流式响应
      await handleSSEStream(response, onChunk);
    } else if (response.body) {
      // 普通流式响应
      await handleReadableStream(response, onChunk);
    } else {
      // 非流式响应，直接返回完整内容
      const data = await response.json();
      onChunk(data.output || data.message || data.response || JSON.stringify(data));
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      console.log('请求已取消');
      return;
    }
    throw error;
  }
}

/**
 * 处理 SSE (Server-Sent Events) 流
 */
async function handleSSEStream(response, onChunk) {
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  try {
    while (true) {
      const { done, value } = await reader.read();
      
      if (done) break;
      
      buffer += decoder.decode(value, { stream: true });
      
      // 处理 SSE 格式的数据
      const lines = buffer.split('\n');
      buffer = lines.pop() || ''; // 保留不完整的行
      
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.slice(6);
          if (data === '[DONE]') {
            return;
          }
          try {
            const parsed = JSON.parse(data);
            const content = parsed.choices?.[0]?.delta?.content || 
                          parsed.content || 
                          parsed.text ||
                          parsed.output ||
                          '';
            if (content) {
              onChunk(content);
            }
          } catch {
            // 如果不是 JSON，直接输出
            if (data.trim()) {
              onChunk(data);
            }
          }
        }
      }
    }
  } finally {
    reader.releaseLock();
  }
}

/**
 * 处理普通的 ReadableStream
 */
async function handleReadableStream(response, onChunk) {
  const reader = response.body.getReader();
  const decoder = new TextDecoder();

  try {
    while (true) {
      const { done, value } = await reader.read();
      
      if (done) break;
      
      const chunk = decoder.decode(value, { stream: true });
      
      // 尝试解析为 JSON
      try {
        const parsed = JSON.parse(chunk);
        const content = parsed.output || parsed.message || parsed.response || parsed.text || chunk;
        onChunk(content);
      } catch {
        // 如果不是 JSON，直接输出文本
        if (chunk.trim()) {
          onChunk(chunk);
        }
      }
    }
  } finally {
    reader.releaseLock();
  }
}

/**
 * 发送非流式消息（备用方案）
 * @param {string} message - 用户消息
 * @param {string} sessionId - 会话ID
 * @returns {Promise<string>}
 */
export async function sendMessage(message, sessionId) {
  const response = await fetch(N8N_WEBHOOK_URL, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      chatInput: MARKDOWN_PROMPT + message,
      sessionId: sessionId,
    }),
  });

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const data = await response.json();
  return data.output || data.message || data.response || JSON.stringify(data);
}

/**
 * 生成唯一的会话ID
 * @returns {string}
 */
export function generateSessionId() {
  return `session_${Date.now()}_${Math.random().toString(36).substring(2, 11)}`;
}

export default {
  sendMessageStream,
  sendMessage,
  generateSessionId,
};
