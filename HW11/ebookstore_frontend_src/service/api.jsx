const API_BASE_URL = '/api';

// 通用请求处理函数
async function fetchApi(endpoint, options = {}) {
  const isGraphql = endpoint.startsWith('/graphql');
  const base = isGraphql ? API_BASE_URL.replace(/\/api$/, '') : API_BASE_URL;
  const url = `${base}${endpoint}`;
  
  // 默认请求头
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  };
    const config = {
    credentials: 'include', // 包含凭据（cookies、sessions等）
    ...options,
    headers
  };
    try {
    const response = await fetch(url, config);
      // 检查响应状态
    if (!response.ok) {
      let errorData = {};
      const contentType = response.headers.get('content-type');
      
      if (contentType && contentType.includes('application/json')) {
        errorData = await response.json().catch(() => ({}));
      } else {
        // 如果响应不是JSON格式，尝试读取文本
        const errorText = await response.text().catch(() => '');
        errorData = { message: errorText || `HTTP ${response.status}` };
      }
      
      // 对于登录接口，即使返回错误状态码，也要返回数据而不是抛出异常
      // 这样前端可以获取到具体的错误信息
      if (endpoint === '/users/login' && errorData) {
        return errorData;
      }
        const error = new Error(errorData.error || errorData.message || `API request failed: ${response.status}`);
      error.status = response.status;
      error.data = errorData;
      throw error;
    }
    
    // 如果是204 No Content，返回null
    if (response.status === 204 || response.headers.get('content-length') === '0') {
      return null;
    }
    
    // 尝试解析JSON响应
    return await response.json();
  } catch (error) {
    console.error('API request error:', error);
    throw error;
  }
}

export default fetchApi;
