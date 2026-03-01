import React, { useState, useEffect, useContext } from 'react'
import { Card, Typography, message, Modal, Button, Alert } from 'antd'
import { ShoppingCartOutlined, ExclamationCircleOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import CartTable from '../../components/cartTable.jsx'
import CartSummary from '../../components/cartSummary.jsx'
import CartService from '../../service/cartService.jsx';
import OrderService from '../../service/orderService.jsx';
import UserService from '../../service/UserService.jsx';
import BookService from '../../service/bookService.jsx';
import { UserContext } from '/src/utils/context';

// Typography组件用于设置标题样式
const { Title } = Typography

const Cart = () => {  const [cartItems, setCartItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [warningModalVisible, setWarningModalVisible] = useState(false)
  const [hasDeletedBooks, setHasDeletedBooks] = useState(false)
  const [deletedBookIds, setDeletedBookIds] = useState([])
  const [hasInsufficientStock, setHasInsufficientStock] = useState(false)
  const [insufficientStockItems, setInsufficientStockItems] = useState([])
  const navigate = useNavigate()
  const { user, setUser } = useContext(UserContext)
  const [messageAPI, contextHolder] = message.useMessage();
  // 新增：订单结果受控状态（避免 static Modal 被重渲染“刷掉”）
  const [orderResult, setOrderResult] = useState(null) // { type: 'success'|'failed', text: '' }

  const fetchCartItems = async () => {
    try {
      setLoading(true);
      
      const account = localStorage.getItem('userAccount');
      if (!account) {
        message.error('请先登录');
        navigate('/login');
        return;
      }
      
      const items = await CartService.getCartItems(account);
      
      // 获取书籍详细信息（包括删除状态）
      const bookIds = items.map(item => item.bookId);
      const booksWithDeleteStatus = await BookService.getBooksByIdsIncludingDeleted(bookIds);
        // 转换为前端需要的格式，并标记已删除的书籍
      const formattedItems = items.map(item => {
        const bookInfo = booksWithDeleteStatus.find(book => book.id === item.bookId);
        return {
          key: item.bookId.toString(),
          bookId: item.bookId,
          name: item.name,
          price: item.price,
          quantity: item.quantity,
          cover: item.cover,
          author: item.author,
          stock: bookInfo ? bookInfo.stock : 0, // 添加库存信息
          deleted: bookInfo ? bookInfo.deleted : false
        };
      });
      
      setCartItems(formattedItems);
        // 检查是否有已删除的书籍
      await checkDeletedBooks();
      
      // 检查库存状态
      await checkCartStock();
    } catch (error) {
      console.error('Error fetching cart items:', error);
      message.error('获取购物车数据失败');
    } finally {
      setLoading(false);
    }
  };
  // 检查购物车中是否有已删除的书籍
  const checkDeletedBooks = async () => {
    try {
      const checkResult = await CartService.checkDeletedBooks();
      setHasDeletedBooks(checkResult.hasDeletedBooks);
      setDeletedBookIds(checkResult.deletedBookIds || []);
    } catch (error) {
      console.error('检查已删除书籍失败:', error);
    }
  };

  // 检查购物车中的库存状态
  const checkCartStock = async () => {
    try {
      const stockResult = await CartService.checkCartStock();
      setHasInsufficientStock(stockResult.hasInsufficientStock);
      setInsufficientStockItems(stockResult.insufficientStockItems || []);
    } catch (error) {
      console.error('检查库存状态失败:', error);
    }
  };

  // 手动刷新购物车和库存状态
  const handleRefreshCart = async () => {
    try {
      setLoading(true);
      await fetchCartItems(); // 这会同时检查删除的书籍和库存状态
      message.success('购物车数据已刷新');
    } catch (error) {
      console.error('刷新购物车失败:', error);
      message.error('刷新失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };
  const handleRemoveDeletedBooks = async () => {
    try {
      await CartService.removeDeletedBooks();
      message.success('已删除的书籍已从购物车中移除');
      
      // 重新获取购物车数据
      await fetchCartItems();
    } catch (error) {
      console.error('清理已删除书籍失败:', error);
      message.error('清理失败');
    }
  };

  // 更新用户余额信息
  const updateUserBalance = async () => {
    try {
      if (user && user.account) {
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

  // 初始化时检查 sessionStorage 是否有未确认的订单结果（防止刷新丢失）
  useEffect(() => {
    try {
      const cache = sessionStorage.getItem('latestOrderResult')
      if (cache) {
        const parsed = JSON.parse(cache)
        if (!parsed.ack && parsed.text) setOrderResult(parsed)
      }
    } catch (e) { /* ignore */ }
  }, [])

  useEffect(() => {
    if (user && user.account) {
        const UserID = user.id;
        const websocket = new WebSocket(`ws://localhost:8080/ws/order?userId=${UserID}`)
        websocket.onopen = () => {
            console.log('WebSocket 连接已建立')
        }
        websocket.onmessage = (event) =>{
            console.log('收到 WebSocket 消息:', event.data)
            const data = event.data.trim()
            // 使用 messageAPI（实例化后的）而不是全局 message
            if (data.match('SUCCESS:')) {
                console.log('WebSocket SUCCESS message:', data);
                const text = data.replace(/^"?SUCCESS:/,'').replace(/"$/,'').trim() || '订单创建成功'
                const payload = { type: 'success', text, ts: Date.now(), ack: false }
                setOrderResult(payload)
                sessionStorage.setItem('latestOrderResult', JSON.stringify(payload))
                // 成功后清空购物车并刷新数据（改为等待 WebSocket SUCCESS 再清空）
                ;(async () => {
                  try {
                    await CartService.clearCart(user.account);
                    await fetchCartItems();
                    await updateUserBalance();
                    message.success('订单创建成功，购物车已清空');
                  } catch (e) {
                    console.error('清空购物车或刷新数据失败:', e);
                  }
                })();
                
            } else if (data.match('FAILED:')) {
                const text = data.replace(/^"FAILED:/,'').trim() || '订单创建失败'
                const payload = { type: 'failed', text, ts: Date.now(), ack: false }
                setOrderResult(payload)
                sessionStorage.setItem('latestOrderResult', JSON.stringify(payload))
            }
        }
        websocket.onclose = () => {
            console.log('WebSocket 连接已关闭')
        }
      fetchCartItems();
    } else {
      setLoading(false);
    }
    // 组件卸载时关闭 WebSocket 连接
    return () => {
      try {
        // websocket 作用域在上方 if 内部；仅当存在时关闭
        if (typeof websocket !== 'undefined' && websocket && websocket.close) {
          websocket.close();
        }
      } catch (e) {
        // ignore
      }
    }
  }, [user]);
  // 处理数量变化
  const handleQuantityChange = async (key, quantity) => {
    if (!user || !user.account) {
      message.warning('请先登录');
      return;
    }

    try {
      const item = cartItems.find(item => item.key === key);
      const response = await CartService.updateCartItem(user.account, item.bookId, quantity);
      
      // 检查响应格式
      if (response && response.success === false) {
        message.error(response.message || '更新数量失败');
        return;
      }
        // 更新本地状态
      setCartItems(prev => 
        prev.map(item => item.key === key ? { ...item, quantity } : item)
      );
      
      // 重新检查库存状态
      await checkCartStock();
      
      message.success('数量已更新');
    } catch (error) {
      console.error('Failed to update quantity:', error);
      // 处理后端返回的错误信息
      if (error.message) {
        message.error(error.message);
      } else {
        message.error('更新数量失败');
      }
    }
  };

  // 处理删除商品
  const handleRemove = async (key) => {
    if (!user || !user.account) {
      message.warning('请先登录');
      return;
    }

    try {
      const item = cartItems.find(item => item.key === key);
      await CartService.removeFromCart(user.account, item.bookId);
      
      // 更新本地状态
      const updatedItems = cartItems.filter(item => item.key !== key);
      setCartItems(updatedItems);
        // 重新检查是否还有已删除的书籍
      const remainingDeletedItems = updatedItems.filter(item => item.deleted);
      if (remainingDeletedItems.length === 0) {
        setHasDeletedBooks(false);
        setDeletedBookIds([]);
      } else {
        setDeletedBookIds(remainingDeletedItems.map(item => item.bookId));
      }
      
      // 重新检查库存状态
      await checkCartStock();
      
      message.success('商品已从购物车中移除');
    } catch (error) {
      console.error('Failed to remove item:', error);
      message.error('删除商品失败');
    }
  };

  // 处理结算
  const handleCheckout = async () => {
    if (!user || !user.account) {
      message.warning('请先登录');
      return;
    }

    if (cartItems.length === 0) {
      message.info('购物车为空');
      return;
    }
      // 检查购物车中是否有已删除的书籍
    if (hasDeletedBooks) {
      message.error('购物车中包含已删除的书籍，请先清理后再下单');
      return;
    }
    
    // 检查购物车中是否有库存不足的书籍
    if (hasInsufficientStock) {
      message.error('购物车中有书籍库存不足，请调整数量或删除相关书籍后再下单');
      return;
    }    // 显示加载状态
    setLoading(true);
    
    try {
      // 在创建订单前，先实时检查一次库存状态
      const stockResult = await CartService.checkCartStock();
      
      // 检查最新的库存状态
      if (stockResult.hasInsufficientStock) {
        // 更新状态
        setHasInsufficientStock(true);
        setInsufficientStockItems(stockResult.insufficientStockItems || []);
        
        message.error('库存状态已发生变化，购物车中有书籍库存不足，请调整后再下单');
        setLoading(false);
        return;
      }
      
      const userProfile = await UserService.getUserProfile(user.account);
      console.log('用户资料：', userProfile); // 添加调试日志
      
      // 检查地址和电话是否为空字符串或null
      if (!userProfile.address || userProfile.address.trim() === '' || 
          !userProfile.phone || userProfile.phone.trim() === '') {
        setLoading(false); // 重置加载状态
        setWarningModalVisible(true); // 显示警告弹窗
        return;
      }
      
      // 创建订单
      await OrderService.createOrder(
        userProfile.address, 
        userProfile.phone
      );
      // 不在这里清空购物车，改为等待 WebSocket 返回 SUCCESS 再清空并刷新
      message.success('订单已提交，请等待系统确认');
        // 导航到订单页面
    //   navigate('/orders');
    } catch (error) {
      console.error('Failed to checkout:', error);
      
      // 检查是否是库存不足或其他购物车相关错误
      const errorMessage = error.message || '未知错误';
      if (errorMessage.includes('库存不足') || 
          errorMessage.includes('已下架') || 
          errorMessage.includes('购物车')) {
        
        // 如果是库存或购物车相关问题，刷新购物车数据
        message.error('结算失败: ' + errorMessage + '，正在刷新购物车数据...');
        
        // 延迟一点时间后刷新，让用户看到错误信息
        setTimeout(async () => {
          try {
            await fetchCartItems(); // 重新获取购物车数据
            message.info('购物车数据已更新，请检查后重新下单');
          } catch (refreshError) {
            console.error('Failed to refresh cart:', refreshError);
            message.error('刷新购物车失败，请手动刷新页面');
          }
        }, 1000);
      } else {
        message.error('结算失败: ' + errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  // 处理警告弹窗的确认按钮 - 跳转到个人信息页面
  const handleWarningOk = () => {
    setWarningModalVisible(false);
    navigate('/checkout');
  };

  // 处理警告弹窗的取消按钮
  const handleWarningCancel = () => {
    setWarningModalVisible(false);
  };

  // 计算总价
  const calculateTotal = () => {
    return cartItems.reduce((total, item) => 
      total + (item.price * item.quantity), 0
    ).toFixed(2);
  };
  return (
    <div style={{ padding: 24 }}>
      {contextHolder}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={3} style={{ margin: 0 }}>
          <ShoppingCartOutlined /> 我的购物车
        </Title>
        {/* <Button 
          type="default" 
          onClick={handleRefreshCart}
          loading={loading}
          icon={<ReloadOutlined />}
        >
          刷新库存
        </Button> */}
      </div>
      
      {/* 已删除书籍警告 */}
      {hasDeletedBooks && (
        <div style={{ marginBottom: 16 }}>
          <Alert
            message="购物车中包含已删除的书籍"
            description="有些书籍已被管理员删除，您需要先清理这些书籍才能下单"
            type="error"
            showIcon
            // action={
            // //   <Button 
            // //     type="primary" 
            // //     size="small" 
            // //     onClick={handleRemoveDeletedBooks}
            // //     icon={<DeleteOutlined />}
            // //   >
            // //     清理已删除书籍
            // //   </Button>
            // }
            closable
            onClose={() => setHasDeletedBooks(false)}
          />
        </div>      )}
      
      {/* 库存不足警告 */}
      {hasInsufficientStock && (
        <div style={{ marginBottom: 16 }}>
          <Alert
            message="购物车中有书籍库存不足"
            description={
              <div>
                <p>以下书籍的购物车数量超过了库存数量：</p>
                <ul style={{ marginBottom: 0 }}>
                  {insufficientStockItems.map((item, index) => (
                    <li key={index}>
                      <strong>{item.bookTitle}</strong> - 
                      购物车数量: {item.requestedQuantity}本，
                      库存剩余: {item.availableStock}本
                    </li>
                  ))}
                </ul>
                <p style={{ marginTop: 8, marginBottom: 0 }}>
                  请调整数量或删除相关书籍后再下单
                </p>
              </div>
            }
            type="warning"
            showIcon
            closable
            onClose={() => setHasInsufficientStock(false)}
          />
        </div>
      )}
      
      <Card>        <CartTable 
          cartItems={cartItems}
          onQuantityChange={handleQuantityChange}
          onRemove={handleRemove}
          insufficientStockItems={insufficientStockItems}
        /><CartSummary 
          total={calculateTotal()}
          onCheckout={handleCheckout}
          disabled={cartItems.length === 0 || hasDeletedBooks || hasInsufficientStock}
        />
      </Card>
      {/* 订单结果受控弹窗，仅在成功/失败时出现 */}
      <Modal
        open={!!orderResult}
        onCancel={() => {
          setOrderResult(null)
          try {
            const cache = sessionStorage.getItem('latestOrderResult')
            if (cache) {
              const parsed = JSON.parse(cache)
              parsed.ack = true
              sessionStorage.setItem('latestOrderResult', JSON.stringify(parsed))
            }
          } catch(e){}
        }}
        onOk={() => {
          setOrderResult(null)
          try {
            const cache = sessionStorage.getItem('latestOrderResult')
            if (cache) {
              const parsed = JSON.parse(cache)
              parsed.ack = true
              sessionStorage.setItem('latestOrderResult', JSON.stringify(parsed))
            }
          } catch(e){}
        }}
        okText="知道了"
        cancelButtonProps={{ style: { display: 'none' } }}
        centered
        destroyOnClose
        title={orderResult?.type === 'success' ? '订单创建成功' : orderResult?.type === 'failed' ? '订单创建失败' : ''}
      >
        <p style={{ whiteSpace: 'pre-line', margin: 0 }}>{orderResult?.text}</p>
      </Modal>

      {/* 个人信息缺失警告弹窗 */}
      <Modal
        title={null}
        open={warningModalVisible}
        onOk={handleWarningOk}
        onCancel={handleWarningCancel}
        okText="去完善信息"
        cancelText="稍后再说"
        centered
        width={400}
        closable={false}
      >
        <div style={{ textAlign: 'center', padding: '20px 0' }}>
          <ExclamationCircleOutlined 
            style={{ 
              fontSize: '48px', 
              color: '#faad14', 
              marginBottom: '16px' 
            }} 
          />
          <Title level={3} style={{ marginBottom: '8px' }}>个人信息不完整</Title>
          <p style={{ color: '#666', marginBottom: '16px' }}>
            检测到您的收货地址或联系电话为空
          </p>
          <p style={{ color: '#999', fontSize: '12px' }}>
            请完善个人信息后再提交订单
          </p>
        </div>
      </Modal>
    </div>
  )
}

export default Cart
