import { Button, Avatar, Typography } from 'antd'
import { LogoutOutlined, UserOutlined, DollarOutlined } from '@ant-design/icons'
import { useContext } from 'react'
import { UserContext } from '/src/utils/context'

// 头部组件，接收两个属性：avatarUrl和onLogout
// avatarUrl用于显示用户头像，onLogout用于处理用户退出登录的逻辑
const Header = ({ avatarUrl, onLogout }) => {
  // 使用useContext获取用户信息 和context上下文有关

  // 当组件调用 useContext(MyContext) 时，
  // React 会从组件向上查找最近的 MyContext.Provider，并使用其 value 属性的值。
  // context变化时，所有与之相关的组件都会重新获取和使用 具体请看useContext
  const {user}  = useContext(UserContext)
  const { Text } = Typography

  return (
    <div style={{ 
      background: '#fff', 
      padding: '0 24px', 
      display: 'flex',
      justifyContent: 'flex-end',
      alignItems: 'center'
    }}>      <div style={{ 
        display: 'flex', 
        alignItems: 'center',
        marginRight: 16
      }}>
        <Avatar 
          size={50} 
          src={avatarUrl} 
          icon={!avatarUrl && <UserOutlined />} 
        />
        <div style={{ marginLeft: 12 }}>
          <div>{user?.name + '，你好！' || '用户，你好！'}</div>
          {user?.balance !== undefined && (
            <Text type="secondary" style={{ fontSize: 12 }}>
              <DollarOutlined /> 余额: ¥{user.balance.toLocaleString()}
            </Text>
          )}
        </div>
      </div>
      {/* // 退出登录按钮，点击时调用onLogout函数 */}
      <Button 
        type="text"
        icon={<LogoutOutlined />}
        onClick={onLogout}
      >
        退出登录
      </Button>
    </div>
  )
}

export default Header