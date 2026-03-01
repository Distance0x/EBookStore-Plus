import { message } from 'antd';
import { useState, useContext } from 'react';
import CartService from '../service/cartService.jsx';
import { UserContext } from '../utils/context.jsx';
import { useNavigate } from 'react-router-dom';


const useCart = () => {
  const [loading, setLoading] = useState(false)
  const { user } = useContext(UserContext)
  const navigate = useNavigate()
  const [messageApi, contextHolder] = message.useMessage()
  const addToCart = async (book, e, quantity = 1) => {
    // 阻止事件冒泡，防止事件冒泡到父组件 调用跳转到详情界面
    e?.stopPropagation()
    
    if (!user || !user.account) {
      messageApi.warning('请先登录再添加商品到购物车');
      navigate('/login');
      return;
    }

    // 检查库存
    if (book.stock !== undefined && book.stock <= 0) {
      messageApi.error(`${book.title} 当前缺货，无法添加到购物车`);
      return;
    }

    if (book.stock !== undefined && quantity > book.stock) {
      messageApi.error(`${book.title} 库存不足，仅剩 ${book.stock} 册`);
      return;
    }    setLoading(true);
    try {
      const response = await CartService.addToCart(user.account, book.id, quantity);
      // 检查响应格式
      if (response && response.success === false) {
        messageApi.error(response.message || '添加到购物车失败');
      } else {
        messageApi.success(`${book.title} 已加入购物车`);
      }
    } catch (error) {
      console.error('Failed to add to cart:', error);
      // 处理后端返回的错误信息
      if (error.message) {
        messageApi.error(error.message);
      } else {
        messageApi.error('添加到购物车失败');
      }
    } finally {
      setLoading(false);
    }
  }

  return { addToCart, cartContextHolder: contextHolder }
}

export default useCart