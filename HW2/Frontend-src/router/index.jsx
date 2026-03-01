import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom'
import { useContext } from 'react'
import { UserContext } from '../utils/context'
import { Spin } from 'antd'
import Home from '../pages/Home'
import Orders from '../pages/Order'
import Statistics from '../pages/Statistics'
import BookDetails from '../pages/BookDetails'
import Login from '../pages/Login'
import Register from '../pages/Register'
import Checkout from '../pages/Checkout'
import Cart from '../pages/Cart'
import AppLayout from '../pages/Layout'
import AdminLayout from '../pages/Admin'
import BookManagement from '../components/BookManagement'
import UserManagement from '../components/UserManagement'
import OrderManagement from '../components/OrderManagement'
import SalesStatistics from '../components/SalesStatistics'
import AdminRoute from '../components/AdminRoute'
import UserConsumptionStatistics from '../components/UserConsumptionStatistics'

// 路由守卫组件 - 基于session状态判断是否登录
const AuthRoute = ({ children }) => {
  const { user, loading } = useContext(UserContext)
  
  // 如果正在加载中，显示加载动画
  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <Spin size="large" tip="正在验证登录状态..." />
      </div>
    )
  }
  
  // 如果用户未登录，重定向到登录页面
  if (!user) {
    return <Navigate to="/login" replace />
  }
  
  // 用户已登录，渲染子组件
  return children
}

// 创建router实例
const router = createBrowserRouter([
  {
    // 一级路由
    path: '/',
    element: <AuthRoute><AppLayout /></AuthRoute>,  
    // 二级路由 
    // 子路由的出现导致不能直接访问二级路由，需要访问一级路由才能访问二级路由
    children: [
      { 
        index: true,  // 设置为默认路由
        element: <Navigate to="/home" replace />  // 重定向到/home
      },      { path: 'home', element: <Home /> },
      { path: 'orders', element: <Orders /> },
      { path: 'statistics', element: <Statistics /> },
      { path: 'books/:id', element: <BookDetails /> },
      { path: 'checkout', element: <Checkout /> },
      { path: 'cart', element: <Cart />}
    ]  
  },  {
    path: '/admin',
    element: <AdminRoute><AdminLayout /></AdminRoute>,
    children: [
      {
        index: true,
        element: <Navigate to="/admin/books" replace />
      },
      { path: 'books', element: <BookManagement /> },
      { path: 'users', element: <UserManagement /> },
      { path: 'orders', element: <OrderManagement /> },
      { path: 'statistics', element: <SalesStatistics /> },
      { path: "/admin/user-consumption", element: <UserConsumptionStatistics />}
    ]
  },
  {
    path: '/login',
    element: <Login />
  },
  {
    path: '/register',
    element: <Register />
  }
])

// 导出router实例，在App.jsx中使用RouterProvider组件进行渲染
export default function AppRouter() {
  return <RouterProvider router={router} />
}