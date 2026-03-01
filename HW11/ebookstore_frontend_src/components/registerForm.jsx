import { Form, Input, Button, message, Alert } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons'
import UserService from '../service/UserService'
import { useState } from 'react'
import { Link } from 'react-router-dom'

const RegisterForm = ({ onRegister }) => {
  const [loading, setLoading] = useState(false)
  const [registerError, setRegisterError] = useState(null)
  const [form] = Form.useForm()

  // 检查用户名是否可用
  const checkUsername = async (_, value) => {
    if (!value) {
      return Promise.resolve()
    }
    
    try {
      const response = await UserService.checkUsername(value)
      if (response && !response.available) {
        return Promise.reject(new Error('用户名已被占用'))
      }
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(new Error('用户名检查失败'))
    }
  }  // 验证确认密码
  const validateConfirmPassword = (_, value) => {
    if (!value) {
      return Promise.resolve()
    }
    const password = form.getFieldValue('password')
    if (!password) {
      return Promise.resolve()
    }
    if (value !== password) {
      return Promise.reject(new Error('两次输入的密码不一致'))
    }
    return Promise.resolve()
  }
  // 处理表单提交
  const handleSubmit = async (values) => {
    try {
      setLoading(true)
      setRegisterError(null)
      
      const { username, password, confirmPassword, email } = values
      
      // 调用注册API
      const response = await UserService.register({
        username,
        password,
        confirmPassword,
        email
      })
      
      if (response && response.success) {
        // 保存用户信息和登录状态
        localStorage.setItem('isLoggedIn', 'true')
        localStorage.setItem('userProfile', JSON.stringify(response.user))
        localStorage.setItem('userAccount', username)
        
        // 调用父组件的注册成功回调
        onRegister(response.user)
      } else {
        setRegisterError(response.message || '注册失败，请重试')
      }
    } catch (error) {
      console.error('Register error:', error)
      setRegisterError(error.message || '注册失败，请检查网络连接后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Form
      form={form}
      name="register"
      onFinish={handleSubmit}
      scrollToFirstError
    >
      {/* 显示错误信息 */}
      {registerError && (
        <Form.Item>
          <Alert
            message="注册失败"
            description={registerError}
            type="error"
            showIcon
            style={{ marginBottom: 16 }}
            closable
            onClose={() => setRegisterError(null)}
          />
        </Form.Item>
      )}

      {/* 用户名输入框 */}
      <Form.Item
        name="username"
        rules={[
          { required: true, message: '请输入用户名!' },
          { min: 3, message: '用户名至少3个字符!' },
          { max: 20, message: '用户名最多20个字符!' },
          { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线!' },
          { validator: checkUsername }
        ]}
        hasFeedback
      >
        <Input 
          prefix={<UserOutlined />} 
          placeholder="用户名 (3-20个字符，支持字母、数字、下划线)" 
        />
      </Form.Item>

      {/* 邮箱输入框 */}
      <Form.Item
        name="email"
        rules={[
          { required: true, message: '请输入邮箱地址!' },
          { type: 'email', message: '请输入有效的邮箱地址!' }
        ]}
        hasFeedback
      >
        <Input 
          prefix={<MailOutlined />} 
          placeholder="邮箱地址" 
        />
      </Form.Item>      {/* 密码输入框 */}
      <Form.Item
        name="password"
        rules={[
          { required: true, message: '请输入密码!' },
          { min: 6, message: '密码至少6个字符!' }
        ]}
        hasFeedback
      >
        <Input.Password
          prefix={<LockOutlined />}
          placeholder="密码 (至少6个字符)"
          onChange={() => {
            // 当密码改变时，重新验证确认密码字段
            if (form.getFieldValue('confirmPassword')) {
              form.validateFields(['confirmPassword'])
            }
          }}
        />
      </Form.Item>

      {/* 确认密码输入框 */}
      <Form.Item
        name="confirmPassword"
        dependencies={['password']}
        rules={[
          { required: true, message: '请确认密码!' },
          { validator: validateConfirmPassword }
        ]}
        hasFeedback
      >
        <Input.Password
          prefix={<LockOutlined />}
          placeholder="确认密码"
        />
      </Form.Item>

      {/* 注册按钮 */}
      <Form.Item>
        <Button 
          type="primary" 
          htmlType="submit" 
          style={{ width: '100%' }}
          loading={loading}
        >
          注册
        </Button>
      </Form.Item>

      {/* 返回登录链接 */}
      <Form.Item style={{ textAlign: 'center', marginBottom: 0 }}>
        <div style={{ 
            display: 'flex', 
            justifyContent: 'center', 
            alignItems: 'center',
            gap: '8px',
            fontSize: '14px'
        }}>
            <span style={{ color: '#000' }}>已有账户？</span>
            <Link 
              to="/login" 
              style={{ 
                color: '#1890ff',
                fontWeight: '500',
                textDecoration: 'none',
                transition: 'color 0.3s ease'
              }}
              onMouseEnter={(e) => e.target.style.color = '#40a9ff'}
              onMouseLeave={(e) => e.target.style.color = '#1890ff'}
            >
              立即登录
            </Link>
        </div>
      </Form.Item>
    </Form>
  )
}

export default RegisterForm
