import fetchApi from "./api";

const OrderService = {
  // 创建订单
  createOrder: async (shippingAddress, contactPhone) => {
    return await fetchApi(
      `/orders/create/kafka?shippingAddress=${shippingAddress}&contactPhone=${contactPhone}`,
      {
        method: "POST",
      }
    );
  }, // 获取用户所有订单（分页）
  getUserOrdersByPage: async (
    page = 0,
    size = 10,
    sortBy = "createTime",
    sortDir = "desc"
  ) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
    });
    return await fetchApi(`/orders?${params}`, {
      method: "GET",
    });
  },

  // 获取用户所有订单（非分页，保留用于兼容）
  getUserOrders: async () => {
    const res = await fetchApi(`/orders/user`, {
      method: "GET",
    });
    console.log(res);
    return res;
  },

  // 获取订单详情
  getOrderById: async (orderId) => {
    return await fetchApi(`/orders/${orderId}`, {
      method: "GET",
    });
  },

  // 获取订单项
  getOrderItems: async (orderId) => {
    return await fetchApi(`/orders/${orderId}/items`, {
      method: "GET",
    });
  },

  // 更新订单状态
  updateOrderStatus: async (orderId, status) => {
    return await fetchApi(`/orders/${orderId}/status?status=${status}`, {
      method: "PUT",
    });
  },

  // 删除订单
  deleteOrder: async (orderId) => {
    return await fetchApi(`/orders/${orderId}`, {
      method: "DELETE",
    });
  },
  // 删除用户所有订单
  deleteAllUserOrders: async () => {
    return await fetchApi(`/orders/user/all`, {
      method: "DELETE",
    });
  },

  // 支付订单
  payOrder: async (orderId) => {
    return await fetchApi(`/orders/${orderId}/pay`, {
      method: "POST",
    });
  },
  // 用户搜索订单（分页）
  searchUserOrdersByPage: async (
    bookTitle,
    startTime,
    endTime,
    page = 0,
    size = 10,
    sortBy = "createTime",
    sortDir = "desc"
  ) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
    });
    if (bookTitle) params.append("bookTitle", bookTitle);
    if (startTime) params.append("startTime", startTime);
    if (endTime) params.append("endTime", endTime);

    return await fetchApi(`/orders/search/page?${params}`, {
      method: "GET",
    });
  },

  // 用户搜索订单（非分页，保留用于兼容）
  searchUserOrders: async (bookTitle, startTime, endTime) => {
    const params = new URLSearchParams();
    if (bookTitle) params.append("bookTitle", bookTitle);
    if (startTime) params.append("startTime", startTime);
    if (endTime) params.append("endTime", endTime);

    const queryString = params.toString();
    const url = `/orders/search${queryString ? `?${queryString}` : ""}`;

    return await fetchApi(url, {
      method: "GET",
    });
  },
  // 取消订单
  cancelOrder: async (orderId) => {
    return await fetchApi(`/orders/${orderId}/cancel`, {
      method: "PUT",
    });
  },
  // 获取用户购买统计（分页）
  getUserPurchaseStatisticsByPage: async (
    startTime,
    endTime,
    page = 0,
    size = 10,
    sortBy = "createTime",
    sortDir = "desc"
  ) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
    });
    if (startTime) params.append("startTime", startTime);
    if (endTime) params.append("endTime", endTime);

    return await fetchApi(`/orders/statistics/page?${params}`, {
      method: "GET",
    });
  },

  // 获取用户购买统计（分页，包含全局统计信息）
  getUserPurchaseStatisticsWithGlobal: async (
    startTime,
    endTime,
    page = 0,
    size = 10,
    sortBy = "createTime",
    sortDir = "desc"
  ) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
      sortBy,
      sortDir,
    });
    if (startTime) params.append("startTime", startTime);
    if (endTime) params.append("endTime", endTime);

    return await fetchApi(`/orders/purchase-statistics-with-global?${params}`, {
      method: "GET",
    });
  },

  // 获取用户购买统计（非分页，保留用于兼容）
  getUserPurchaseStatistics: async (startTime, endTime) => {
    const params = new URLSearchParams();
    if (startTime) params.append("startTime", startTime);
    if (endTime) params.append("endTime", endTime);

    const queryString = params.toString();
    const url = `/orders/statistics${queryString ? `?${queryString}` : ""}`;

    return await fetchApi(url, {
      method: "GET",
    });
  },

  // 更新订单联系信息
  updateOrderContactInfo: async (orderId, address, phone) => {
    return await fetchApi(`/orders/${orderId}/contact`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        address: address,
        phone: phone,
      }),
    });
  },
};

export default OrderService;
