import { 
  HomeOutlined,
  BookOutlined,
  ShoppingCartOutlined,
  UserOutlined,
  SettingOutlined,
  BarChartOutlined
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { Layout as AntdLayout, message } from 'antd'
import { Outlet } from 'react-router-dom'
import { useState, useContext } from 'react'
import Sidebar from '../../components/layoutSidebar.jsx'
import Header from '../../components/layoutHeader.jsx'
import UserService from '../../service/UserService.jsx'
import { UserContext } from '../../utils/context.jsx'

const { Sider, Content } = AntdLayout

const AppLayout = () => {
  // Hooks只能在组件顶层或自定义Hook中调用
  const navigate = useNavigate()
  const { user, setUser } = useContext(UserContext)
  const [avatarUrl] = useState('/src/assets/image/a.jpg')
    // 根据用户角色动态生成菜单项
  const getMenuItems = () => {
    const baseItems = [
      { key: '1', icon: <HomeOutlined />, label: '首页', path: '/home' },
      { key: '2', icon: <ShoppingCartOutlined />, label: '购物车', path: '/cart' },
      { key: '3', icon: <BookOutlined />, label: '订单', path: '/orders' },
      { key: '4', icon: <BarChartOutlined />, label: '购买统计', path: '/statistics' },
      { key: '5', icon: <UserOutlined />, label: '个人中心', path: '/checkout'}
    ]      // 如果是管理员，添加管理后台入口
    if (user?.role === 'admin') {
      baseItems.push({
        key: '6', 
        icon: <SettingOutlined />, 
        label: '管理后台', 
        path: '/admin/books'
      })
    }
    
    return baseItems
  }
  
  const handleLogout = async () => {
    try {
      // 调用后端logout API
      await UserService.logout()
      message.success('退出登录成功')
    } catch (error) {
      console.error('Logout error:', error)
      // 即使API调用失败，也要清除本地数据
      message.warning('退出登录')
    } finally {
      // 清除所有本地存储的用户数据
      localStorage.removeItem('isLoggedIn')
      localStorage.removeItem('userProfile')
      localStorage.removeItem('userAccount')
      // 重置用户上下文状态
      setUser(null)
      navigate('/login')
    }
  }
  return (
    <AntdLayout style={{ minHeight: '100vh' }}>
      <Sider width={200}>
        <Sidebar menuItems={getMenuItems()} />
      </Sider>
      <AntdLayout>
        <Header avatarUrl={avatarUrl} onLogout={handleLogout} />
        <Content style={{ margin: '24px 16px 0', flex: 1, overflow: 'auto' }}>
          <div style={{ padding: 24, background: '#fff', minHeight: 'calc(100vh - 112px)'}}>
            <Outlet />
          </div>
        </Content>
      </AntdLayout>
    </AntdLayout>
  )
}

export default AppLayout







// import React, { useState, useEffect } from'react';
// import { Layout as AntdLayout, Menu, Button, Avatar } from 'antd' 
// import { useNavigate, Outlet, useLocation } from 'react-router-dom'
// import { 
//   HomeOutlined, 
//   BookOutlined, 
//   ShoppingCartOutlined,
//   LogoutOutlined,
//   UserOutlined
// } from '@ant-design/icons';
// import { useContext } from 'react';
// import { UserContext } from '/src/utils/context';

// const { Header, Content, Sider } = AntdLayout;

// const AppLayout = () => {
//   const { user } = useContext(UserContext);
//   // 将useState移到组件内部
//   const [avatarUrl, setAvatarUrl] = useState('/src/assets/image/a.jpg');
//   const navigate = useNavigate();
//   const location = useLocation();
  
//   // 从localStorage获取用户数据
//   const userProfile = JSON.parse(localStorage.getItem('userProfile') || '{}');
//   const menuItems = [
//     { key: '1', icon: <HomeOutlined />, label: '首页', path: '/home' },
//     { key: '2', icon: <ShoppingCartOutlined />, label: '购物车', path: '/cart' },
//     { key: '3', icon: <BookOutlined />, label: '订单', path: '/orders' },
//     { key: '4', icon: <UserOutlined />, label: '个人中心', path: '/checkout'}
//   ];

//   // 将getSelectedKey移到组件内部
//   const getSelectedKey = () => {
//     const currentItem = menuItems.find(item => item.path === location.pathname);
//     return currentItem ? [currentItem.key] : ['1'];
//   };

//   const handleLogout = () => {
//     localStorage.removeItem('isLoggedIn')
//     navigate('/login')
//   }
//   return (
//     <AntdLayout style={{ minHeight: '100vh' }}>
//       {/* // sider 侧边栏 */}
//       <Sider width={200}>
//       <div style={{ 
//         height: '32px', 
//         margin: '16px', 
//         background: 'rgba(255, 255, 255, 0.2)',
//         color: '#fff', // 添加白色文字
//         display: 'flex',
//         alignItems: 'center',
//         justifyContent: 'center',
//         fontSize: '16px',
//         fontWeight: 'bold'
//       }}>
//     ebookstore
//   </div>
//         <Menu
//           theme="dark"
//           mode="inline"
//           selectedKeys={getSelectedKey()}
//           items={menuItems.map(item => ({
//             ...item,
//             onClick: () => navigate(item.path)
//           }))}
//         />
//       </Sider>
//       <AntdLayout>
//         <Header style={{ 
//           background: '#fff', 
//           padding: '0 24px', 
//           display: 'flex',
//           justifyContent: 'flex-end',
//           alignItems: 'center'
//         }}>
//           <div style={{ 
//             display: 'flex', 
//             alignItems: 'center',
//             marginRight: 16
//           }}>
//             <Avatar 
//             size={50} 
//             src={avatarUrl} 
//             icon={!avatarUrl && <UserOutlined />} 
//           />
//             <span>{user?.name + '，你好！' || '用户，你好！'}</span>
//           </div>
//           <Button 
//             type="text"
//             icon={<LogoutOutlined />}
//             onClick={handleLogout}
//           >
//             退出登录
//           </Button>
//         </Header>
//         {/*内容区域 */}
//         <Content style={{ 
//           margin: '24px 16px 0',
//           flex : 1,
//           overflow : 'auto'
//         }}>
//           <div style={{ padding: 24, background: '#fff', minHeight: 'calc(100vh - 112px)'}}>
//             {/* <div>这是home</div> */}
//             <Outlet />
//             {/* 根据outlet跳转  */}
//           </div>
//         </Content>
//       </AntdLayout>
//     </AntdLayout>
//   )
// }

// export default AppLayout;
