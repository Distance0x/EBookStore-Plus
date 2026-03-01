import fetchApi from './api.jsx'

const AdminService = {
  // 书籍管理
  createBook: async (bookData) => {
    return await fetchApi('/admin/books', {
      method: 'POST',
      body: JSON.stringify(bookData)
    })
  },

  updateBook: async (bookId, bookData) => {
    return await fetchApi(`/admin/books/${bookId}`, {
      method: 'PUT',
      body: JSON.stringify(bookData)
    })
  },

  deleteBook: async (bookId) => {
    return await fetchApi(`/admin/books/${bookId}`, {
      method: 'DELETE'
    })
  },
  // 用户管理
  getAllUsers: async () => {
    return await fetchApi('/admin/users')
  },

  // 用户管理（分页）
  getUsersByPage: async (page = 0, size = 10, sortBy = 'id', sortDir = 'asc') => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });
    return await fetchApi(`/admin/users/page?${params}`)
  },

  updateUserStatus: async (userId, status) => {
    return await fetchApi(`/admin/users/${userId}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status })
    })
  },  // 订单管理（分页）
  getAllOrdersByPage: async (page = 0, size = 10, sortBy = 'createTime', sortDir = 'desc') => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });
    return await fetchApi(`/admin/orders/page?${params}`)
  },

  // 订单管理（非分页，保留用于兼容）
  getAllOrders: async () => {
    return await fetchApi('/admin/orders')
  },
    // 管理员搜索订单（分页）
  searchAllOrdersByPage: async (bookTitle, startTime, endTime, page = 0, size = 10, sortBy = 'createTime', sortDir = 'desc') => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });
    if (bookTitle) params.append('bookTitle', bookTitle);
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    return await fetchApi(`/admin/orders/search/page?${params}`, {
      method: 'GET'
    });
  },
  
  // 管理员搜索订单（非分页，保留用于兼容）
  searchAllOrders: async (bookTitle, startTime, endTime) => {
    const params = new URLSearchParams();
    if (bookTitle) params.append('bookTitle', bookTitle);
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    const queryString = params.toString();
    const url = `/admin/orders/search${queryString ? `?${queryString}` : ''}`;
    
    return await fetchApi(url, {
      method: 'GET'
    });
  },    // 获取书籍销量统计（分页）
  getBookSalesStatisticsByPage: async (startTime, endTime, page = 0, size = 10, sortBy = 'totalSales', sortDir = 'desc') => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    return await fetchApi(`/admin/statistics/books/page?${params}`, {
      method: 'GET'
    });
  },

  // 获取书籍销量统计（分页，包含全局统计信息）
  getBookSalesStatisticsWithGlobal: async (startTime, endTime, page = 0, size = 10, sortBy = 'totalSales', sortDir = 'desc') => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    return await fetchApi(`/admin/statistics/books-sales-with-global?${params}`, {
      method: 'GET'
    });
  },

  // 获取书籍销量统计（非分页，保留用于兼容）
  getBookSalesStatistics: async (startTime, endTime) => {
    const params = new URLSearchParams();
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    const queryString = params.toString();
    const url = `/admin/statistics/books${queryString ? `?${queryString}` : ''}`;
    
    return await fetchApi(url, {
      method: 'GET'
    });
  },
  // 获取用户消费统计（分页）
  getUserConsumptionStatisticsByPage: async (startTime, endTime, page = 0, size = 10, sortBy = 'totalAmount', sortDir = 'desc') => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    return await fetchApi(`/admin/statistics/users/page?${params}`, {
      method: 'GET'
    });
  },

  // 获取用户消费统计（分页，包含全局统计信息）
  getUserConsumptionStatisticsWithGlobal: async (startTime, endTime, page = 0, size = 10, sortBy = 'totalAmount', sortDir = 'desc') => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    return await fetchApi(`/admin/statistics/user-consumption-with-global?${params}`, {
      method: 'GET'
    });
  },

  // 获取用户消费统计（非分页，保留用于兼容）
  getUserConsumptionStatistics: async (startTime, endTime) => {
    const params = new URLSearchParams();
    if (startTime) params.append('startTime', startTime);
    if (endTime) params.append('endTime', endTime);
    
    const queryString = params.toString();
    const url = `/admin/statistics/users${queryString ? `?${queryString}` : ''}`;
    
    return await fetchApi(url, {
      method: 'GET'
    });
  }

}

export default AdminService
