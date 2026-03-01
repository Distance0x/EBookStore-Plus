import { 
  BookOutlined,
  UserOutlined,
  ShoppingCartOutlined,
  ArrowLeftOutlined,
  BarChartOutlined,
  TeamOutlined
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { Layout as AntdLayout, message, Button } from 'antd'
import { Outlet } from 'react-router-dom'
import { useState, useContext } from 'react'
import Sidebar from '../../components/layoutSidebar.jsx'
import Header from '../../components/layoutHeader.jsx'
import UserService from '../../service/UserService.jsx'
import { UserContext } from '../../utils/context.jsx'
import useAdminSessionCheck from '../../hooks/useAdminSessionCheck.jsx'

const { Sider, Content } = AntdLayout

const AdminLayout = () => {
  // 使用管理员session检查hook
  useAdminSessionCheck();
  
  const navigate = useNavigate()
  const { setUser } = useContext(UserContext)
  const [avatarUrl] = useState('/src/assets/image/a.jpg')
  const menuItems = [
    { key: '1', icon: <BookOutlined />, label: '书籍管理', path: '/admin/books' },
    { key: '2', icon: <UserOutlined />, label: '用户管理', path: '/admin/users' },
    { key: '3', icon: <ShoppingCartOutlined />, label: '订单管理', path: '/admin/orders' },
    { key: '4', icon: <BarChartOutlined />, label: '销量统计', path: '/admin/statistics' },
    { key: '5', icon: <TeamOutlined />, label: '消费统计', path: '/admin/user-consumption' }
  ]
    const handleLogout = async () => {
    try {
      await UserService.logout()
      message.success('退出登录成功')
    } catch (error) {
      console.error('Logout error:', error)
      message.warning('退出登录')
    } finally {
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('userProfile')
      localStorage.removeItem('userAccount')
      setUser(null)
      navigate('/login')
    }
  }

  const handleBackToMain = () => {
    navigate('/home')
  }

  return (
    <AntdLayout style={{ minHeight: '100vh' }}>
      <Sider width={200}>
        <div style={{ 
          height: 32, 
          margin: 16, 
          background: 'rgba(255, 255, 255, 0.2)',
          borderRadius: 6,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: 'white',
          fontWeight: 'bold'
        }}>
          管理后台
        </div>
        <Sidebar menuItems={menuItems} />
      </Sider>      <AntdLayout>
        <Header avatarUrl={avatarUrl} onLogout={handleLogout} />
        <Content style={{ margin: '24px 16px 0', flex: 1, overflow: 'auto' }}>
          <div style={{ 
            padding: '16px 24px 24px', 
            background: '#fff', 
            minHeight: 'calc(100vh - 112px)'
          }}>
            <Button 
              type="primary" 
              icon={<ArrowLeftOutlined />} 
              onClick={handleBackToMain}
              style={{ marginBottom: '16px' }}
            >
              返回主界面
            </Button>
            <Outlet />
          </div>
        </Content>
      </AntdLayout>
    </AntdLayout>
  )
}

export default AdminLayout
