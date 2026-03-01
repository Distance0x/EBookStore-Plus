import { Menu } from 'antd'
import { useNavigate, useLocation } from 'react-router-dom'

const Sidebar = ({ menuItems }) => {
  const navigate = useNavigate()
  // 用于获取当前页面的路由位置信息
  const location = useLocation()
  // 获取当前选中的key
  const getSelectedKey = () => {
    const currentItem = menuItems.find(item => item.path === location.pathname)
    return currentItem ? [currentItem.key] : ['1']
  };

  return (
    <>
      <div style={{ 
        height: '32px', 
        margin: '16px', 
        background: 'rgba(255, 255, 255, 0.2)',
        color: '#fff',
        // flex布局，垂直居中显示文字，水平居中显示自己
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: '16px',
        fontWeight: 'bold'
      }}>
        ebookstore
      </div>
      <Menu
        theme="dark" // 深色主题
        // inline表示自带折叠/展开功能 默认垂直
        mode="inline"
        selectedKeys={getSelectedKey()}
        // 遍历menuItems，将每个item的onClick属性设置为navigate(item.path)
        items={menuItems.map(item => ({
          ...item,
          onClick: () => navigate(item.path)
        }))}
      />
    </>
  )
}

export default Sidebar