import fetchApi from './api';

const BookService = {
  // 用户端：获取所有图书（只返回未删除的）
  getAllBooks: async () => {
    return fetchApi('/books');
  },    // 用户端：分页获取图书（只返回未删除的）
  getBooksByPage: async (page = 0, size = 8, sortBy = 'id', sortDir = 'asc') => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir
    });
    return fetchApi(`/books/page?${params}`);
  },
  
  // 用户端：获取图书详情（只返回未删除的）
  getBookById: async (id) => {
    return fetchApi(`/books/${id}`);
  },
  
  // 用户端：搜索图书（只搜索未删除的）
  searchBooks: async (keyword) => {
    return fetchApi(`/books/search?keyword=${encodeURIComponent(keyword)}`);
  },
    // 购物车和订单用：获取图书详情（包括已删除的）
  getBookByIdIncludingDeleted: async (id) => {
    return fetchApi(`/books/cart/${id}`);
  },
  
  // 购物车和订单用：批量获取图书（包括已删除的）
  getBooksByIdsIncludingDeleted: async (ids) => {
    return fetchApi('/books/cart/batch', {
      method: 'POST',
      body: JSON.stringify(ids)
    });
  },
  
  // 管理员功能：创建图书
  createBook: async (bookData) => {
    return fetchApi('/books/admin', {
      method: 'POST',
      body: JSON.stringify(bookData)
    });
  },
  
  // 管理员功能：更新图书信息
  updateBook: async (id, bookData) => {
    return fetchApi(`/books/admin/${id}`, {
      method: 'PUT',
      body: JSON.stringify(bookData)
    });
  },
    // 管理员功能：软删除图书
  deleteBook: async (id) => {
    return fetchApi(`/books/admin/${id}`, {
      method: 'DELETE'
    });
  }
};

export default BookService;