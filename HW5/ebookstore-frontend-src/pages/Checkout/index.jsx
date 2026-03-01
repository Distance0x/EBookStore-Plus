import React, { useState, useEffect, useContext } from 'react'
import { Form, Card, Typography, message } from 'antd'
import { UserContext } from '/src/utils/context'
import UserProfileForm from '../../components/userProfileForm'
import UserService from '../../service/UserService'

const { Title } = Typography
// 个人信息页面
// 加载用户信息，处理保存操作
// 表单提交后，保存用户信息到本地存储，并更新UserContext
const Checkout = () => {
  // 创建一个表单实例
  const [form] = Form.useForm()
  // UserContext是一个ctx对象 可以解构出setUser方法
  const { user, setUser } = useContext(UserContext)
  const [loading, setLoading] = useState(false)
  const [avatarUrl, setAvatarUrl] = useState('/src/assets/image/a.jpg')

  const [initialFormValues, setInitialFormValues] = useState({
    name: 'Tom',
    email: 'Tom@example.com',
    phone: '13800138000',
    address: '上海市闵行区江川路街道东川路800号'
  })
  const loadInitialValues = async () => {
    
    try {
      setLoading(true);
      const account = localStorage.getItem('userAccount');
      
      if (account) {
        try {
          // 尝试从API获取最新数据
          const userData = await UserService.getUserProfile(account);
          form.setFieldsValue(userData)
          setInitialFormValues(userData)
          
          // 如果有头像，设置头像URL
          if (userData.avatar) {
            setAvatarUrl(userData.avatar);
          }
          
          // 更新上下文和本地存储
          setUser(userData);
          localStorage.setItem('userProfile', JSON.stringify(userData));
        } catch (error) {
          console.error('Failed to fetch user profile:', error);
          
          // 失败时使用已有数据
          if (user) {
            form.setFieldsValue(user);
            if (user.avatar) {
              setAvatarUrl(user.avatar);
            }
          } else {
            // 使用默认值
            const defaultValues = {
              name: 'Tom',
              email: 'Tom@example.com',
              phone: '13800138000',
              address: '上海市闵行区江川路街道东川路800号'
            };
            form.setFieldsValue(defaultValues);
          }
        }
      } else {
        // 使用上下文中的数据或默认值
        if (user) {
          form.setFieldsValue(user);
          if (user.avatar) {
            setAvatarUrl(user.avatar);
          }
        } else {
          // 使用默认值
          const defaultValues = {
            name: 'Tom',
            email: 'Tom@example.com',
            phone: '13800138000',
            address: '上海市闵行区江川路街道东川路800号'
          };
          form.setFieldsValue(defaultValues);
        }
      }
    } catch (error) {
      console.error('Error loading profile:', error);
      message.error('加载用户信息失败');
    } finally {
      setLoading(false);
    }
  };
  // 这里用来加载初始值的逻辑
  useEffect(() => {
    loadInitialValues()
  }, []);

  // 渲染用户信息表单
  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Title level={4} style={{ marginBottom: 24 }}>个人账户信息</Title>
        <UserProfileForm
          form={form}
          loading={loading}
          setLoading={setLoading}
          avatarUrl={avatarUrl}
          setUser={setUser}
          initialValues={initialFormValues}
          // onFinish={onFinish}
        />
      </Card>
    </div>
  )
}

export default Checkout




  // const loadInitialValues = () => {
  //   const savedData = localStorage.getItem('userProfile')
  //   if (savedData) {
  //     const data = JSON.parse(savedData)
  //     return data
  //   }
  //   return {
  //     name: 'Tom',
  //     email: 'Tom@example.com',
  //     phone: '13800138000',
  //     address: '上海市闵行区江川路街道东川路800号',
  //     avatar: null
  //   }
  // }
  // // 修改useEffect，添加依赖项
  // // 确保只有form变化时才执行
  // useEffect(() => {
  //   const initialValues = loadInitialValues()
  //   form.setFieldsValue(initialValues)
  //   if (initialValues.avatar) {
  //     setAvatarUrl(initialValues.avatar)
  //   }
  // }, [form])
  // // 修改onFinish函数
  // // 保存用户信息到本地存储，并更新UserContext
  // // 同时，在保存成功后显示消息提示
  // // 这个函数在表单成功验证并提交后被调用
  // const onFinish = (values) => {
  //   setLoading(true)
  //   const currentData = JSON.parse(localStorage.getItem('userProfile') || '{}')
  //   const newUserData = {
  //     ...currentData,
  //     ...values,
  //     avatar: avatarUrl
  //   }
  //   localStorage.setItem('userProfile', JSON.stringify(newUserData))
  //   setUser(newUserData)
  //   setTimeout(() => {
  //     setLoading(false)
  //   }, 500)
  // }





// import React, { useState, useEffect, useContext } from 'react'
// import { Card, Form, Input, Button, Typography, message, Upload, Avatar } from 'antd'
// import { UserOutlined, MailOutlined, PhoneOutlined, UploadOutlined } from '@ant-design/icons'
// import { UserContext } from '/src/utils/context'

// const { Title } = Typography

// const Checkout = () => {
//   const [form] = Form.useForm()
//   const { setUser } = useContext(UserContext)
//   // UserContext是一个ctx对象 可以解构出setUser方法
//   const [loading, setLoading] = useState(false)
//   const [avatarUrl, setAvatarUrl] = useState('/src/assets/image/a.jpg')

//   // 修改loadInitialValues函数，移除setAvatarUrl调用
//   const loadInitialValues = () => {
//     const savedData = localStorage.getItem('userProfile')
//     if (savedData) {
//       const data = JSON.parse(savedData);
//       return data;
//     }
//     return {
//       name: 'Tom',
//       email: 'Tom@example.com',
//       phone: '13800138000',
//       address: '上海市闵行区江川路街道东川路800号',
//       avatar: null
//     };
//   };

//   // 修改useEffect，添加依赖项
//   useEffect(() => {
//     const initialValues = loadInitialValues()
//     form.setFieldsValue(initialValues)
//     if (initialValues.avatar) {
//       setAvatarUrl(initialValues.avatar)
//     }
//   }, [form]); // 确保只有form变化时才执行

//   // 修改onFinish函数
//   const onFinish = (values) => {
//     setLoading(true)
//     // 从localStorage获取当前数据（包含可能已上传的头像）
//     const currentData = JSON.parse(localStorage.getItem('userProfile') || '{}')
//     const newUserData = {  // 正确定义新数据对象
//         ...currentData,
//         ...values,
//         avatar: avatarUrl  // 确保头像URL也被保存
//     }
//     localStorage.setItem('userProfile', JSON.stringify(newUserData))
  
//     // 修改这里 - 传递完整的用户对象而不是只传name
//     setUser(newUserData); 
//     setTimeout(() => {
//       message.success('个人信息已保存')
//       setLoading(false)
//     }, 500)
//   }

//   return (
//     <div style={{ padding: 24 }}>
//       <Card>
//         <Title level={4} style={{ marginBottom: 24 }}>个人账户信息</Title>
//         <div style={{ textAlign: 'center', marginBottom: 24 }}>
//           <Avatar 
//             size={100} 
//             src={avatarUrl} 
//             icon={!avatarUrl && <UserOutlined />} 
//           />
//         </div>
//         <Form
//           form={form}
//           initialValues={loadInitialValues()}
//           onFinish={onFinish}
//           layout="vertical"
//         >
//           <Form.Item
//             name="name"
//             label="姓名"
//             rules={[{ required: true, message: '请输入姓名' }]}
//           >
//             <Input prefix={<UserOutlined />} placeholder="请输入姓名" />
//           </Form.Item>

//           <Form.Item
//             name="email"
//             label="邮箱"
//             rules={[
//               { required: true, message: '请输入邮箱' },
//               { type: 'email', message: '请输入有效的邮箱地址' }
//             ]}
//           >
//             <Input prefix={<MailOutlined />} placeholder="请输入邮箱" />
//           </Form.Item>

//           <Form.Item
//             name="phone"
//             label="手机号"
//             rules={[
//               { required: true, message: '请输入手机号' },
//               { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
//             ]}
//           >
//             <Input prefix={<PhoneOutlined />} placeholder="请输入手机号" />
//           </Form.Item>

//           <Form.Item
//             name="address"
//             label="收货地址"
//             rules={[{ required: true, message: '请输入收货地址' }]}
//           >
//             <Input.TextArea rows={4} placeholder="请输入详细地址" />
//           </Form.Item>

//           <Form.Item>
//             <Button type="primary" htmlType="submit" loading={loading}>
//               保存修改
//             </Button>
//           </Form.Item>
//         </Form>
//       </Card>
//     </div>
//   )
// }

// export default Checkout;