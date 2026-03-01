import fetchApi from './api';

const CartService = {
  // 获取购物车列表
  getCartItems: async (account) => {
    return await fetchApi(`/cart?account=${account}`, {
      method: 'GET'
    });
  },
  
  // 添加商品到购物车
  addToCart: async (account, bookId, quantity) => {
    return await fetchApi('/cart/add', {
      method: 'POST',
      body: JSON.stringify({
        account,
        bookId,
        quantity: quantity || 1
        // 截断设置 不合法的数据设置为 1
      })
    });
  },
  
  // 更新购物车中的商品数量
  updateCartItem: async (account, bookId, quantity) => {
    return await fetchApi('/cart/update', {
      method: 'PUT',
      body: JSON.stringify({
        account,
        bookId,
        quantity
      })
    });
  },
  
  // 从购物车中移除商品
  removeFromCart: async (account, bookId) => {
    return await fetchApi(`/cart/remove?account=${account}&bookId=${bookId}`, {
      method: 'DELETE'
    });
  },
  
  // 清空购物车
  clearCart: async (account) => {
    return await fetchApi(`/cart/clear?account=${account}`, {
      method: 'DELETE'
    });
  },
  
  // 检查购物车中是否有已删除的书籍
  checkDeletedBooks: async () => {
    return await fetchApi('/cart/check-deleted', {
      method: 'GET'
    });
  },
    // 清理购物车中已删除的书籍
  removeDeletedBooks: async () => {
    return await fetchApi('/cart/remove-deleted', {
      method: 'DELETE'
    });
  },

  // 检查购物车中的库存状态
  checkCartStock: async () => {
    return await fetchApi('/cart/check-stock', {
      method: 'GET'
    });
  }
};

export default CartService;