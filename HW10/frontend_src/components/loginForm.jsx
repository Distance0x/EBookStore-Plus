import { Form, Input, Button, message, Alert } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import UserService from '../service/UserService';
import { useState } from 'react';
import { Link } from 'react-router-dom';

const LoginForm = ({onLogin}) => {
  const [loading, setLoading] = useState(false);
  const [loginError, setLoginError] = useState(null); // 添加错误状态

  const handleSubmit = async (values) => {
    try {
      setLoading(true);
      setLoginError(null); // 清除之前的错误信息
      const { username, password } = values;
      
      // 调用API登录
      const response = await UserService.login(username, password);
      
      if (response && response.success) {
        // 保存用户信息和登录状态
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('userProfile', JSON.stringify(response.user));
        localStorage.setItem('userAccount', username);
        
        // 调用父组件的登录成功回调
        message.success('登录成功');
        onLogin(response.user);
      } else {
        // 根据后端返回的具体错误信息显示
        const errorMessage = response.message || '登录失败，请重试';
        setLoginError(errorMessage);
        message.error(errorMessage);
      }
    } catch (error) {
      console.error('Login error:', error);
      
      // 尝试从错误响应中获取具体的错误信息
      let errorMessage = '登录失败，请检查网络连接';
      
      if (error.response && error.response.data) {
        if (error.response.data.message) {
          errorMessage = error.response.data.message;
        } else if (typeof error.response.data === 'string') {
          errorMessage = error.response.data;
        }
      }
      
      setLoginError(errorMessage);
      message.error(errorMessage);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Form
      name="normal_login"
      initialValues={{ remember: true }}
      onFinish={handleSubmit}
    >
      {/* 显示错误信息的Alert组件 */}
      {loginError && (
        <Form.Item>
          <Alert
            message="登录失败"
            description={loginError}
            type="error"
            showIcon
            style={{ marginBottom: 16 }}
          />
        </Form.Item>
      )}
      <Form.Item
        name="username"
        rules={[{ required: true, message: '请输入用户名!' }]}
      >
        <Input 
          prefix={<UserOutlined />} 
          placeholder="用户名" 
        />
      </Form.Item>
      <Form.Item
        name="password"
        rules={[{ required: true, message: '请输入密码!' }]}
      >
        <Input
          prefix={<LockOutlined />}
          type="password"
          placeholder="密码"
        />
      </Form.Item>
      <Form.Item>
        <Button 
          type="primary" 
          htmlType="submit" 
          style={{ width: '100%' }}
          loading={loading}
        >
          登录
        </Button>
      </Form.Item>

      <Form.Item style={{ textAlign: 'center', marginBottom: 0 }}>
        <div style={{ 
            display: 'flex', 
            justifyContent: 'center', 
            alignItems: 'center',
            gap: '8px',
            fontSize: '14px'
        }}>
            <span style={{ color: '#000' }}>还没有账户？</span>
            <Link 
            to="/register" 
            style={{ 
                color: '#1890ff',
                fontWeight: '500',
                textDecoration: 'none',
                transition: 'color 0.3s ease'
            }}
            onMouseEnter={(e) => e.target.style.color = '#40a9ff'}
            onMouseLeave={(e) => e.target.style.color = '#1890ff'}
            >
            立即注册
            </Link>
        </div>
        </Form.Item>
        </Form>
  )
}

export default LoginForm