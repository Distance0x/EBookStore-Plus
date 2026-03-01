import React from 'react'
import { useState, useEffect, useContext  } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { message } from 'antd'
import BookDetail from '../../components/bookDetailsCard'
import bookData from '/src/assets/data/book.js'
import BookService from '../../service/bookService.jsx'
import CartService from '../../service/cartService.jsx'
import useSessionCheck from '../../hooks/useSessionCheck.jsx'
import { UserContext } from '../../utils/context.jsx'
import { Spin } from 'antd';

const Bookdetails = () => {
  // useParams 获取路由参数 ( /books/:id )
  const { id } = useParams()
  const navigate = useNavigate()
  const [book, setBook] = useState(null)
  const [loading, setLoading] = useState(true)
  const [addingToCart, setAddingToCart] = useState(false)
  const [error, setError] = useState(null)
  const { user } = useContext(UserContext)
  // 使用session检查Hook进行页面级别的session验证
  useSessionCheck()
  // message 用于显示消息提示组件
  const [messageApi, contextHolder] = message.useMessage()

  useEffect(() => {
    fetchBookDetails();
  }, [id]);
  
  const fetchBookDetails = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await BookService.getBookById(id)
      setBook(data)
    } catch (error) {
      console.error('Failed to fetch book details:', error)
      setError(error)
      if (error.status === 404) {
        message.error('图书不存在或已被删除')
      } else {
        message.error('获取图书详情失败，请稍后再试')
      }
    } finally {
      setLoading(false)
    }
  }
  
  // 直接查找书籍数据
  // const book = bookData.find(book => book.id === parseInt(id))
  
  // if (!book) {
  //   // 如果没有找到书籍，跳转到404页面
  //   navigate('/404')
  //   return null
  // }
  // 添加到购物车的逻辑
  // 1. 从localStorage中获取购物车数据，如果没有则初始化为空数组
  // 2. 查找购物车中是否已经存在该书籍，如果存在则增加数量，否则添加新的书籍
  // 3. 将更新后的购物车数据保存回localStorage中
  // 4. 显示添加成功的消息
  // const addToCart = () => {
  //   const cartItems = JSON.parse(localStorage.getItem('cartItems') || '[]')
  //   const existingItem = cartItems.find(item => item.key === book.id.toString())
    
  //   if (existingItem) {
  //     existingItem.quantity += 1
  //   } else {
  //     // 如果不存在则添加新的书籍
  //     cartItems.push({
  //       key: book.id.toString(),
  //       name: book.title,
  //       price: book.price,
  //       quantity: 1,
  //       cover: book.cover
  //     })
  //   }

  //   localStorage.setItem('cartItems', JSON.stringify(cartItems));
  //   // 显示添加成功的消息
  //   messageApi.success(`${book.title} 已加入购物车`);
  // }
  const addToCart = async () => {
    if (!user || !user.account) {
      messageApi.warning('请先登录再添加商品到购物车');
      navigate('/login');
      return;
    }
    
    setAddingToCart(true);
    try {
      await CartService.addToCart(user.account, book.id, 1);
      messageApi.success(`${book.title} 已加入购物车`);
    } catch (error) {
      console.error('Failed to add to cart:', error);
      messageApi.error('添加到购物车失败');
    } finally {
      setAddingToCart(false);
    }
  }

  // 添加条件渲染
  if (loading) {
    return <div style={{ padding: 24 }}><Spin tip="加载中..." /></div>
  }
  
  if (error) {
    return <div style={{ padding: 24 }}>
      {contextHolder}
      <div>出错了：{error.message || '未知错误'}</div>
      <button onClick={() => navigate(-1)}>返回</button>
    </div>
  }
  
  if (!book) {
    return <div style={{ padding: 24 }}>
      {contextHolder}
      <div>未找到图书</div>
      <button onClick={() => navigate(-1)}>返回</button>
    </div>
  }

  return (
    <div style={{ padding: 24 }}>
      {/* // contextHolder 用于渲染消息提示组件 */}
      {contextHolder}
      <BookDetail 
        book={book}
        onAddToCart={addToCart}
        onBack={() => navigate(-1)}
      />
    </div>
  )
}

export default Bookdetails;



// import React, { useState, useEffect } from 'react'
// import { useParams, useNavigate } from 'react-router-dom'
// import { Card, Button, Typography, Image, message, Space } from 'antd'
// import { ShoppingCartOutlined } from '@ant-design/icons'
// import bookData from '/src/assets/data/book.js'

// const { Title, Text } = Typography

// // useParams 获取路由参数 ( /books/:id )
// const Bookdetails = () => {
//   const { id } = useParams();
//   const navigate = useNavigate();
//   const [messageApi, contextHolder] = message.useMessage();
  
//   // 直接查找书籍数据
//   const book = bookData.find(book => book.id === parseInt(id));
  
//   // 如果没有找到书籍，跳转到404页面
//   if (!book) {
//     navigate('/404');
//     return null; // 防止继续渲染
//   }

//   const addToCart = () => {
//     const cartItems = JSON.parse(localStorage.getItem('cartItems') || '[]');
//     const existingItem = cartItems.find(item => item.key === book.id.toString())
    
//     if (existingItem) {
//       existingItem.quantity += 1;
//     } else {
//       cartItems.push({
//         key: book.id.toString(),
//         name: book.title,
//         price: book.price,
//         quantity: 1,
//         cover: book.cover
//       })
//     }

//     localStorage.setItem('cartItems', JSON.stringify(cartItems));
//     messageApi.success(`${book.title} 已加入购物车`)
//   }

//   return (
//     <div style={{ padding: 24 }}>
//       {contextHolder}
//       <Card>
//         <div style={{ display: 'flex', gap: 24 }}>
//           <Image
//             width={300}
//             src={book.cover}
//             alt={book.title}
//             style={{ borderRadius: 8 }}
//           />
//           <div style={{ flex: 1 }}>
//             <Title level={2}>{book.title}</Title>
//             <Text type="secondary" style={{ fontSize: 16, display: 'block', marginBottom: 16 }}>
//               作者: {book.author}
//             </Text>
//             <Text strong style={{ fontSize: 24, color: '#ff4d4f', display: 'block', marginBottom: 16 }}>
//               ¥{book.price}
//             </Text>
//             <Text style={{ marginBottom: 24 }}>{book.description}</Text>
//             <Space>
//               <Button 
//                 type="primary" 
//                 size="large"
//                 onClick={addToCart}
//               >
//                 加入购物车
//               </Button>
//               {/* // navigate(-1) 可以返回上一页 */}
//               <Button size="large" onClick={() => navigate(-1)}>
//                 返回
//               </Button>
//             </Space>
//           </div>
//         </div>
//       </Card>
//     </div>
//   )
// }

// export default Bookdetails