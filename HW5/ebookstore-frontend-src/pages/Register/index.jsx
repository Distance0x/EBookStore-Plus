import { Card, Typography, Modal } from 'antd'
import { useState, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import { CheckCircleOutlined } from '@ant-design/icons'
import loginBg from '../../assets/image/login.jpg'
import RegisterForm from '../../components/registerForm.jsx'

import { UserContext } from '../../utils/context.jsx'

const { Title } = Typography;

const Register = () => {
  const navigate = useNavigate()
  const { setUser } = useContext(UserContext)
  const [successModalVisible, setSuccessModalVisible] = useState(false)
  
  // 处理注册成功
  const handleRegister = (userData) => {
    setUser(userData)
    setSuccessModalVisible(true)
    
    // 3秒后自动跳转到登录页
    setTimeout(() => {
      setSuccessModalVisible(false)
      navigate('/login')
    }, 3000)
  }

  // 手动确认跳转
  const handleModalOk = () => {
    setSuccessModalVisible(false)
    navigate('/login')
  }
  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      backgroundImage: `url(${loginBg})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center'
    }}>
      <Card style={{ 
        width: 600,
        backdropFilter: 'blur(10px)',
        backgroundColor: 'rgba(255, 255, 255, 0.3)',
        border: 'none',
        boxShadow: '0 8px 32px 0 rgba(31, 38, 135, 0.18)'
      }}>
        <Title level={2} style={{ textAlign: 'center', marginBottom: 30 }}>用户注册</Title>
        <RegisterForm onRegister={handleRegister} />
      </Card>

      {/* 注册成功弹窗 */}
      <Modal
        title={null}
        open={successModalVisible}
        onOk={handleModalOk}
        onCancel={handleModalOk}
        okText="立即登录"
        cancelText="稍后再说"
        centered
        width={400}
        closable={false}
      >
        <div style={{ textAlign: 'center', padding: '20px 0' }}>
          <CheckCircleOutlined 
            style={{ 
              fontSize: '48px', 
              color: '#52c41a', 
              marginBottom: '16px' 
            }} 
          />
          <Title level={3} style={{ marginBottom: '8px' }}>注册成功！</Title>
          <p style={{ color: '#666', marginBottom: '16px' }}>
            恭喜您成功注册账户，即将为您跳转到登录页面
          </p>
          <p style={{ color: '#999', fontSize: '12px' }}>
            页面将在 3 秒后自动跳转...
          </p>
        </div>
      </Modal>
    </div>
  )
}

export default Register
