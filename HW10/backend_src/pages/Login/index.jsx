import { Card, Typography, message } from 'antd'
import { useState, useContext } from 'react'
import { useNavigate } from 'react-router-dom'
import loginBg from '../../assets/image/login.jpg'
import LoginForm from '../../components/loginForm.jsx'

import { UserContext } from '../../utils/context.jsx'

const { Title } = Typography;

const Login = () => {
  const navigate = useNavigate()
  const { setUser } = useContext(UserContext)

  // 请求登录
  const handleLogin = (UserData) =>{
    setUser(UserData)
    // 成功后导航到首页
    navigate('/')
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
        <Title level={2} style={{ textAlign: 'center', marginBottom: 30 }}>电子书城</Title>
        <LoginForm onLogin={handleLogin} />
      </Card>
    </div>
  )
}

export default Login



  // // 模拟登录请求 必须填写用户名和密码 否则登录失败
  // const handleLogin = async (credentials) => {
  //   return new Promise((resolve, reject) => {
  //     setTimeout(() => {
  //       const { username, password } = credentials
  //       if (username && password) {
  //         resolve()
  //       } else {
  //         reject(new Error('用户名或密码不能为空'))
  //       }
  //     }, 500)
  //   })
  // }
  // // async 函数用于处理登录逻辑，接收表单提交的值，调用handleLogin函数进行登录验证
  // const onLogin = async (values) => {
  //   try {
  //     setLoading(true)
  //     await handleLogin(values)
  //     localStorage.setItem('isLoggedIn', 'true')
  //     navigate('/')
  //   } catch (error) {
  //     message.error(error.message || '登录失败，请重试')
  //   } finally {
  //     setLoading(false)
  //   }
  // }

// import { Card, Form, Input, Button, Typography, message } from 'antd';
// import { UserOutlined, LockOutlined } from '@ant-design/icons';
// import { useNavigate } from 'react-router-dom'; // 添加路由跳转hook
// import { useState } from 'react';
// import loginBg from '../../assets/image/login.jpg'
// // 标题获取
// const { Title } = Typography;


// const Login = () => {
//   // 定义完成函数,用于处理表单提交
//   const [loading, setLoading] = useState(false)
//   const navigate = useNavigate(); // 获取路由跳转方法


//   // 使用异步函数处理登录逻辑
//   // 接收表单提交的值
//   // 调用handleLogin函数进行登录验证
//   const onFinish = async (values) => {
//     try {
//       // 模拟异步登录请求
//       setLoading(true)
//       await handleLogin(values)
      
//       // 登录成功提示
//       message.success('登录成功！')
      
//       // 跳转到首页
//       setLoading(true)
//       localStorage.setItem('isLoggedIn', 'true') // 登录成功设置标记
//       navigate('/')
//     } 
//     catch (error) {
//       // 登录失败处理
//       message.error(error.message || '登录失败，请重试');
//     }

//     finally {
//       setLoading(false) // 添加finally重置状态
//     }
//   };
//   // 模拟登录请求
//   const handleLogin = async (credentials) => {
//     return new Promise((resolve, reject) => {
//       // 模拟API请求延迟
//       setTimeout(() => {
//         const { username, password } = credentials;
        
//         // 简单验证逻辑（实际项目中应调用真实API）
//         if (username && password) {
//           resolve()
//         } else {
//           reject(new Error('用户名或密码不能为空'))
//         }
//       }, 500)
//     })
//   }

//   return (
//     <div style={{
//       display: 'flex',
//       justifyContent: 'center',
//       alignItems: 'center',
//       minHeight: '100vh',
//       backgroundImage: `url(${loginBg})`,
//       backgroundSize: 'cover',
//       backgroundPosition: 'center'
//     }}>
//       <Card style={{ 
//         width: 600,
//         backdropFilter: 'blur(10px)',
//         backgroundColor: 'rgba(255, 255, 255, 0.3)', // 修改透明度为0.3
//         border: 'none', // 移除边框
//         boxShadow: '0 8px 32px 0 rgba(31, 38, 135, 0.18)' // 调浅阴影
//       }}>
//         <Title level={2} style={{ textAlign: 'center', marginBottom: 30 }}>电子书城</Title>
//         <Form
//           name="normal_login"
//           initialValues={{ remember: true }}
//           // 表单提交时调用onFinish函数
//           onFinish={onFinish}
//         >
//           <Form.Item
//             name="username"
//             rules={[{ required: true, message: '请输入用户名!' }]}
//           >
//             <Input 
//               prefix={<UserOutlined />} 
//               placeholder="用户名" 
//             />
//           </Form.Item>
//           <Form.Item
//             name="password"
//             rules={[{ required: true, message: '请输入密码!' }]}
//           >
//             <Input
//               prefix={<LockOutlined />}
//               type="password"
//               placeholder="密码"
//             />
//           </Form.Item>
//           <Form.Item>
//             <Button 
//               type="primary" 
//               htmlType="submit" 
//               style={{ width: '100%' }}
//               // 当loading为true时，按钮会显示加载图标并禁用
//               loading={loading} // 控制按钮加载状态
//             >
//               登录
//             </Button>
//           </Form.Item>
//         </Form>
//       </Card>
//     </div>
//   );
// }

// export default Login