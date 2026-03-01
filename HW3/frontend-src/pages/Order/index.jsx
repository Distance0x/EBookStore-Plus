import React, { useState, useEffect, useContext } from 'react'
import { message, Pagination } from 'antd'
import OrderList from '../../components/orderList.jsx'
import OrderSearch from '../../components/orderSearch.jsx'
import EditContactModal from '../../components/EditContactModal.jsx'
import InsufficientBalanceModal from '../../components/InsufficientBalanceModal.jsx'
import OrderService from '../../service/orderService.jsx'
import UserService from '../../service/UserService.jsx'
import { UserContext } from '../../utils/context.jsx'

// 订单管理页面
// 加载订单数据，处理支付和删除操作
const Orders = () => {  const [orders, setOrders] = useState([])
  const [isPayModalOpen, setIsPayModalOpen] = useState(false)
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false)
  const [isCancelModalOpen, setIsCancelModalOpen] = useState(false) // 新增：取消订单模态框状态
  const [isEditContactModalOpen, setIsEditContactModalOpen] = useState(false) // 新增：修改联系信息模态框状态
  const [isInsufficientBalanceModalOpen, setIsInsufficientBalanceModalOpen] = useState(false) // 新增：余额不足模态框状态
  const [currentOrderId, setCurrentOrderId] = useState(null)
  const [currentOrderData, setCurrentOrderData] = useState(null) // 新增：当前编辑的订单数据
  const [currentOrderAmount, setCurrentOrderAmount] = useState(0) // 新增：当前订单金额
  const [loading, setLoading] = useState(false)
  const [searchLoading, setSearchLoading] = useState(false)
  
  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize] = useState(10)
  const [totalOrders, setTotalOrders] = useState(0)
  const [isSearchMode, setIsSearchMode] = useState(false)
  const [searchParams, setSearchParams] = useState({})
  
  // 获取用户上下文，用于更新余额
  const { user, setUser } = useContext(UserContext)

  // 获取用户ID - 从localStorage获取当前用户信息
  const getUserId = async () => {
    const userInfo = localStorage.getItem('userProfile'); // 修改为正确的键名
    if (userInfo) {
      try {
        const user = JSON.parse(userInfo); // 直接解析用户对象
        if (user && user.id) {
          return user.id; // 直接返回用户ID
        } else if (user && user.account) {
          // 如果没有id但有account，则通过API获取用户信息
          const userDetail = await UserService.getUserProfile(user.account);
          return userDetail.id;
        }
      } catch (error) {
        console.error('解析用户信息失败:', error);
        return null;
      }
    }
    return null;
  }  // 分页加载订单
  const loadOrdersByPage = async (page) => {
    console.log(`Loading orders for page: ${page}`);
    setLoading(true);
    try {
      const userId = await getUserId();
      console.log('userId:', userId)
      if (!userId) {
        message.error('请先登录');
        return;
      }

      const data = await OrderService.getUserOrdersByPage(page - 1, pageSize);
      
      // 为每个订单获取详情
      const ordersWithDetails = await Promise.all(
        data.content.map(async (order) => {
          try {
            // 获取订单详情
            const orderDetails = await OrderService.getOrderItems(order.id);
            return {
              id: order.id,
              total: order.total,
              status: order.status, 
              Time: new Date(order.createTime).toLocaleString(),
              address: order.address,
              phone: order.phone,
              orderNumber: order.orderNumber,
              orderItems: orderDetails && orderDetails.length > 0 ? orderDetails[0].orderItems || [] : []
            };
          } catch (error) {
            console.error(`获取订单 ${order.id} 详情失败:`, error);
            return {
              id: order.id,
              total: order.total,
              status: order.status, 
              Time: new Date(order.createTime).toLocaleString(),
              address: order.address,
              phone: order.phone,
              orderNumber: order.orderNumber,
              orderItems: []
            };
          }
        })
      );
      
      setOrders(ordersWithDetails);
      setTotalOrders(data.totalElements);
      setCurrentPage(page);
    } catch (error) {
      console.error('加载订单失败:', error);
      message.error('加载订单失败: ' + (error.message || '未知错误'));
    } finally {
      setLoading(false);
    }
  }

  const loadOrders = async () => {
    console.log('loadOrders called');
    setLoading(true);
    try {
      const userId = await getUserId();
      console.log('userId:', userId)
      if (!userId) {
        message.error('请先登录');
        return;
      }

      const data = await OrderService.getUserOrders();
      
      // 为每个订单获取详情
      const ordersWithDetails = await Promise.all(
        data.map(async (order) => {
          try {
            // 获取订单详情
            const orderDetails = await OrderService.getOrderItems(order.id);
            return {
              id: order.id,
              total: order.total,
              status: order.status, 
              Time: new Date(order.createTime).toLocaleString(),
              address: order.address,
              phone: order.phone,
              orderNumber: order.orderNumber,
              orderItems: orderDetails && orderDetails.length > 0 ? orderDetails[0].orderItems || [] : []
            };
          } catch (error) {
            console.error(`获取订单 ${order.id} 详情失败:`, error);
            // 如果获取详情失败，返回基本信息
            return {
              id: order.id,
              total: order.total,
              status: order.status, 
              Time: new Date(order.createTime).toLocaleString(),
              address: order.address,
              phone: order.phone,
              orderNumber: order.orderNumber,
              orderItems: []
            };
          }
        })
      );
      
      setOrders(ordersWithDetails);
    } catch (error) {
      console.error('加载订单失败:', error);
      message.error('加载订单失败: ' + (error.message || '未知错误'));
    } finally {
      setLoading(false);
    }
  };
    useEffect(() => {
    loadOrdersByPage(1);
  }, []);

  // 搜索订单功能
  const searchOrders = async (searchParams, page = 1) => {
    console.log('searchOrders called with params:', searchParams);
    setSearchLoading(true);
    try {
      const userId = await getUserId();
      if (!userId) {
        message.error('请先登录');
        return;
      }

      let data;
      // 检查是否有搜索条件
      if (!searchParams.bookTitle && !searchParams.startTime && !searchParams.endTime) {
        // 无搜索条件，切换回分页模式
        setIsSearchMode(false);
        data = await OrderService.getUserOrdersByPage(page - 1, pageSize);
      } else {
        // 有搜索条件，使用搜索API（分页）
        setIsSearchMode(true);
        setSearchParams(searchParams);
        data = await OrderService.searchUserOrdersByPage(
          searchParams.bookTitle,
          searchParams.startTime,
          searchParams.endTime,
          page - 1,
          pageSize
        );
      }
      
      // 为每个订单获取详情
      const ordersWithDetails = await Promise.all(
        data.content.map(async (order) => {
          try {
            // 获取订单详情
            const orderDetails = await OrderService.getOrderItems(order.id);
            return {
              id: order.id,
              total: order.total,
              status: order.status, 
              Time: new Date(order.createTime).toLocaleString(),
              address: order.address,
              phone: order.phone,
              orderNumber: order.orderNumber,
              orderItems: orderDetails && orderDetails.length > 0 ? orderDetails[0].orderItems || [] : []
            };
          } catch (error) {
            console.error(`获取订单 ${order.id} 详情失败:`, error);
            return {
              id: order.id,
              total: order.total,
              status: order.status, 
              Time: new Date(order.createTime).toLocaleString(),
              address: order.address,
              phone: order.phone,
              orderNumber: order.orderNumber,
              orderItems: []
            };
          }
        })
      );
      
      setOrders(ordersWithDetails);
      setTotalOrders(data.totalElements);
      setCurrentPage(page);
      
      if (searchParams.bookTitle || searchParams.startTime || searchParams.endTime) {
        message.success(`找到 ${data.totalElements} 个符合条件的订单`);
      }
    } catch (error) {
      console.error('搜索订单失败:', error);
      message.error('搜索订单失败: ' + (error.message || '未知错误'));
    } finally {
      setSearchLoading(false);
    }
  };

  // 处理分页变化
  const handlePageChange = (page) => {
    if (isSearchMode) {
      searchOrders(searchParams, page);
    } else {
      loadOrdersByPage(page);
    }
  };
  // 更新用户余额信息
  const updateUserBalance = async () => {
    try {
      const userId = await getUserId();
      if (userId && user) {
        // 重新获取用户信息以更新余额
        const updatedUser = await UserService.getUserProfile(user.account);
        setUser(updatedUser);
        // 同时更新localStorage中的用户信息
        localStorage.setItem('userProfile', JSON.stringify(updatedUser));
      }
    } catch (error) {
      console.error('更新用户余额失败:', error);
    }
  };
  // 处理订单搜索
  const handleSearch = async (searchParams) => {
    console.log('搜索参数:', searchParams);
    searchOrders(searchParams, 1); // 使用新的分页搜索函数，重置到第1页
  };const handlePay = (orderId) => {
    setCurrentOrderId(orderId)
    setIsPayModalOpen(true)
  };  // 确认支付处理
  const handlePayOk = async () => {
    try {
      // 使用正确的支付API，它会自动扣减库存和余额
      await OrderService.payOrder(currentOrderId);
      message.success(`订单 ${currentOrderId} 支付成功`);
      // 重新加载订单列表
      loadOrders();
      // 更新用户余额信息
      await updateUserBalance();
    } catch (error) {
      console.error('支付失败:', error);
      
      // 检查是否是余额不足错误
      if (error.message && error.message.includes('账户余额不足')) {
        // 解析错误信息获取订单金额
        const order = orders.find(o => o.id === currentOrderId);
        const orderAmount = order?.total || 0;
        
        // 显示余额不足模态框
        setCurrentOrderAmount(orderAmount);
        setIsInsufficientBalanceModalOpen(true);
      } else {
        // 其他错误直接显示错误消息
        message.error('支付失败: ' + (error.message || '未知错误'));
      }
    } finally {
      setIsPayModalOpen(false);
      setCurrentOrderId(null);
    }
  };

  // 取消支付处理
  const handleCancelPay = () => {
    setIsPayModalOpen(false)
    setCurrentOrderId(null)
  }
  
  // 处理取消订单按钮点击
  const handleCancelOrder = (orderId) => {
    setCurrentOrderId(orderId)
    setIsCancelModalOpen(true)
  }
  
  // 确认取消订单处理
  const handleCancelOrderOk = async () => {
    try {
      await OrderService.cancelOrder(currentOrderId);
      message.success(`订单 ${currentOrderId} 已取消，库存已恢复`);
      // 重新加载订单列表
      loadOrders();
    } catch (error) {
      console.error('取消订单失败:', error);
      message.error('取消订单失败: ' + (error.message || '未知错误'));
    } finally {
      setIsCancelModalOpen(false);
      setCurrentOrderId(null);
    }
  };

  // 取消取消订单处理
  const handleCancelOrderCancel = () => {
    setIsCancelModalOpen(false)
    setCurrentOrderId(null)
  }
  
  // 处理删除所有订单按钮点击
  const handleDeleteAll = () => {
    setIsDeleteModalOpen(true) // 打开删除弹窗
  }
  // 确认删除所有订单
  const handleDeleteOk = async () => {
    try {
      const userId = await getUserId();
      if (!userId) {
        message.error('请先登录');
        return;
      }
      
      await OrderService.deleteAllUserOrders(userId);
      setOrders([]);
      message.success('已删除所有订单');
    } catch (error) {
      console.error('删除订单失败:', error);
      message.error('删除订单失败: ' + (error.message || '未知错误'));
    } finally {
      setIsDeleteModalOpen(false);
    }
  };
  const handleCancelDelete = () => {
    setIsDeleteModalOpen(false)
    message.info('已取消删除')
  }

  // 处理修改联系信息按钮点击
  const handleEditContact = (orderData) => {
    setCurrentOrderData(orderData)
    setIsEditContactModalOpen(true)
  }

  // 处理联系信息提交
  const handleContactSubmit = async (contactData) => {
    try {
      await OrderService.updateOrderContactInfo(currentOrderData.id, contactData.address, contactData.phone)
      message.success('联系信息更新成功')
      // 重新加载订单列表
      loadOrders()
    } catch (error) {
      console.error('更新联系信息失败:', error)
      throw new Error(error.message || '更新失败，请重试')
    }
  }
  // 关闭修改联系信息模态框
  const handleCloseEditContactModal = () => {
    setIsEditContactModalOpen(false)
    setCurrentOrderData(null)
  }

  // 处理余额不足模态框关闭
  const handleCloseInsufficientBalanceModal = () => {
    setIsInsufficientBalanceModalOpen(false)
    setCurrentOrderId(null)
    setCurrentOrderAmount(0)
  }

  // 处理去充值按钮点击
  const handleRecharge = () => {
    setIsInsufficientBalanceModalOpen(false)
    setCurrentOrderId(null)
    setCurrentOrderAmount(0)
    message.info('请联系管理员进行余额充值')
    // 这里可以跳转到充值页面或显示充值说明
  }

  return (
    <div style={{ padding: 24 }}>
      <OrderSearch
        onSearch={handleSearch}
        loading={searchLoading}
      />      <OrderList
        orders={orders}
        onPay={handlePay}
        onCancel={handleCancelOrder} // 新增：取消订单处理函数
        onEditContact={handleEditContact} // 新增：修改联系信息处理函数
        // onDeleteAll={handleDeleteAll}
        currentOrderId={currentOrderId}
        isPayModalOpen={isPayModalOpen}
        // isDeleteModalOpen={isDeleteModalOpen}
        isCancelModalOpen={isCancelModalOpen} // 新增：取消订单模态框状态
        onPayOk={handlePayOk}
        onCancelPay={handleCancelPay}
        onCancelOk={handleCancelOrderOk} // 新增：确认取消订单处理
        onCancelCancel={handleCancelOrderCancel} // 新增：取消取消订单处理
        // onDeleteOk={handleDeleteOk}
        // onCancelDelete={handleCancelDelete}
        loading={loading}
      />
      
      {/* 分页组件 */}
      {!loading && orders.length > 0 && (
        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <Pagination
            current={currentPage}
            total={totalOrders}
            pageSize={pageSize}
            onChange={handlePageChange}
            showSizeChanger={false}
            showQuickJumper
            showTotal={(total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条订单`
            }
          />
        </div>
      )}
        <EditContactModal
        isOpen={isEditContactModalOpen}
        onClose={handleCloseEditContactModal}
        onSubmit={handleContactSubmit}
        initialData={{
          address: currentOrderData?.address || '',
          phone: currentOrderData?.phone || ''
        }}
      />
      
      <InsufficientBalanceModal
        isOpen={isInsufficientBalanceModalOpen}
        onClose={handleCloseInsufficientBalanceModal}
        currentBalance={user?.balance || 0}
        requiredAmount={currentOrderAmount}
        onRecharge={handleRecharge}
      />
    </div>
  )
}
// handlePay 是一个事件处理函数，它的返回值不会被 React 用于渲染
// React 只会使用组件函数 ( Orders ) 的 return 值来渲染界面
export default Orders


// import React, { useState, useEffect } from 'react'
// import { Table, Button, Modal, Tag, Space, message } from 'antd'
// import { CheckOutlined, ExclamationCircleOutlined } from '@ant-design/icons'

// // 解构出confirm方法 - 不再需要直接解构 confirm，但保留 Modal
// // const { confirm } = Modal; 

// const Orders = () => {
//     const [orders, setOrders] = useState([]) // 订单数据
    

//     const [isPayModalOpen, setIsPayModalOpen] = useState(false)
//     const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false) // 使用这个状态
//     const [currentOrderId, setCurrentOrderId] = useState(null); 

//     // 处理支付按钮点击
//     const handlePay = (orderId) => {
//       setCurrentOrderId(orderId); 
//       setIsPayModalOpen(true); 
//     };

//     // 处理模态框确认支付
//     const handlePayOk = () => {
//       const updatedOrders = orders.map(order => 
//         order.id === currentOrderId ? { ...order, status: '已完成' } : order
//       );
//       setOrders(updatedOrders);
//       localStorage.setItem('orders', JSON.stringify(updatedOrders));
//       setIsPayModalOpen(false); 
//       setCurrentOrderId(null); 
//       message.success(`订单 ${currentOrderId} 支付成功`);
//     };

//     // 处理模态框取消
//     const handleCancelPay = () => {
//       setIsPayModalOpen(false); 
//       setCurrentOrderId(null); 
//     };

//     // 修改 handleDeleteAll：只打开删除确认弹窗
//     const handleDeleteAll = () => {
//       setIsDeleteModalOpen(true); // 打开删除确认弹窗
//     };

//     // 新增：处理删除确认弹窗的确认按钮
//     const handleDeleteOk = () => {
//       setOrders([]);
//       localStorage.removeItem('orders');
//       setIsDeleteModalOpen(false); // 关闭弹窗
//       message.success('已删除所有订单');
//     };

//     // 新增：处理删除确认弹窗的取消按钮
//     const handleCancelDelete = () => {
//       setIsDeleteModalOpen(false); // 关闭弹窗
//       message.info('已取消删除');
//     };

//       // 加载数据
//       useEffect(() => {
//           const savedOrders = localStorage.getItem('orders')
//           if (savedOrders) {
//               try {
//                 const parsedOrders = JSON.parse(savedOrders)
//                 if (Array.isArray(parsedOrders)) {
//                   setOrders(parsedOrders)// 直接设置全新数组
//                 }
//               } catch (error) {
//                 console.error('订单数据解析失败:', error)
//                 message.error('订单数据加载失败')
//               }
//             }

//     }, [])

//     const columns =  [
//         {
//             title: '订单编号',
//             dataIndex: 'id',
//             key: 'id',
//         },
//         {
//             title: '总金额',
//             dataIndex: 'total',
//             key: 'total',
//             render: total => `¥${total}`,
//         },
//         {
//             title: '状态',
//             dataIndex: 'status',
//             key: 'status',
//             render: status => (
//               <Tag color={status === '已完成' ? 'green' : 'orange'}>
//                 {status}
//               </Tag>
//             ),
//         },
//         {
//             title: '创建时间',
//             dataIndex: 'Time',
//             key: 'Time',
//           },
//           {
//             title: '操作',
//             key: 'action',
//             render: (_, record) => (
//               <Space>
//                 <Button 
//                   type="primary" 
//                   icon={<CheckOutlined />}
//                   onClick={() => handlePay(record.id)} // Pass only the ID
//                   disabled={record.status === '已完成'}
//                 >
//                   {record.status === '已完成' ? '已完成' : '立即支付'}
//                 </Button>
//               </Space>
//             ),
//           }
//     ]

//     return(
//       <div style={{ padding: 24 }}>
//         <Table 
//           columns={columns} 
//           dataSource={orders} 
//           rowKey="id"
//           footer={() => (
//             <div style={{ textAlign: 'right' }}>
//               <Button 
//                 type="primary" 
//                 danger
//                 onClick={handleDeleteAll} // onClick 现在指向修改后的 handleDeleteAll
//                 disabled={orders.length === 0}
//               >
//                 删除所有订单
//               </Button>
//             </div>
//           )}
//         />
        
//         {/* 支付确认 Modal */}
//         <Modal 
//           title="支付确认" 
//           open={isPayModalOpen} 
//           onOk={handlePayOk} 
//           onCancel={handleCancelPay} 
//           okText="确认支付"
//           cancelText="取消"
//         >
//           <p>您确定要支付订单 {currentOrderId} 吗？</p>
//         </Modal>

//         {/* 新增：删除确认 Modal */}
//         <Modal
//           title="确认删除"
//           open={isDeleteModalOpen} // 由 isDeleteModalOpen 控制
//           onOk={handleDeleteOk} // 确认按钮调用 handleDeleteOk
//           onCancel={handleCancelDelete} // 取消按钮调用 handleCancelDelete
//           okText="确认"
//           cancelText="取消"
//           okButtonProps={{ danger: true }} // 让确认按钮变红以示警告
//         >
//           <p><ExclamationCircleOutlined style={{ color: 'red', marginRight: 8 }} />确定要删除所有订单吗？此操作不可恢复。</p>
//         </Modal>
//       </div>
//     )
// }
// // handlePay 是一个事件处理函数，它的返回值不会被 React 用于渲染
// // React 只会使用组件函数 ( Orders ) 的 return 值来渲染界面
// export default Orders