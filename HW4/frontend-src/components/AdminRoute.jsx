import { useContext } from 'react'
import { Navigate } from 'react-router-dom'
import { UserContext } from '../utils/context'
import { Result, Button, Spin } from 'antd'

const AdminRoute = ({ children }) => {
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
        <Spin size="large" tip="正在验证管理员权限..." />
      </div>
    )
  }
  
  // 如果用户未登录，重定向到登录页面
  if (!user) {
    return <Navigate to="/login" replace />
  }
  
  // 如果不是管理员，显示403页面
  if (user.role !== 'admin') {
    return (
      <Result
        status="403"
        title="403"
        subTitle="抱歉，您没有权限访问此页面。"
        extra={
          <Button type="primary" onClick={() => window.history.back()}>
            返回
          </Button>
        }
      />
    )
  }
  
  // 用户已登录且是管理员，渲染子组件
  return children
}

export default AdminRoute
