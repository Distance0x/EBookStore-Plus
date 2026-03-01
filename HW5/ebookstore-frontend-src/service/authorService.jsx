const GATEWAY_BASE_URL = 'http://localhost:8080';

// 作者服务API - 通过gateway访问微服务
const AuthorService = {
  /**
   * 根据书名查询作者
   * @param {string} title - 书名
   * @param {boolean} exact - 是否精确匹配，默认false（模糊匹配）
   * @returns {Promise} 返回作者信息
   */
  getAuthorByTitle: async (title, exact = false) => {
    const params = new URLSearchParams({
      title: title,
      exact: exact.toString()
    });
    
    const url = `${GATEWAY_BASE_URL}/author/api/authors/by-title?${params}`;
    
    try {
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      return await response.json();
    } catch (error) {
      console.error('Failed to fetch author:', error);
      throw error;
    }
  }
};

export default AuthorService;

