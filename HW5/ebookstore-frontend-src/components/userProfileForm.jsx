import { Form, Input, Button, Avatar, message } from 'antd';
import { UserOutlined, MailOutlined, PhoneOutlined } from '@ant-design/icons';
import { useState, useContext } from 'react';
import { UserContext } from '../utils/context';
import UserService from '../service/UserService';

const UserProfileForm = ({ 
  form,
  avatarUrl,
  loading,
  setLoading,
  setUser,
  initialValues,
}) => {
  const onFinish = async (values) => {
    try {
      setLoading(true);
      
      // 获取当前用户账号
      const account = localStorage.getItem('userAccount');
      if (!account) {
        message.error('未登录或会话已过期');
        return;
      }
      
      // 添加头像URL（如果有）
      const profileData = {
        ...values,
        avatar: avatarUrl
      };
      
      // 调用API更新资料
      const updatedUser = await UserService.updateUserProfile(account, profileData);
      
      // 与您的UserContext保持一致，直接设置用户数据到localStorage
      localStorage.setItem('userProfile', JSON.stringify(updatedUser));
      
      // 更新Context
      setUser(updatedUser);
      
      message.success('个人信息已保存');
    } catch (error) {
      console.error('Update profile error:', error);
      message.error('保存失败: ' + (error.message || '请检查网络连接'));
    } finally {
      setLoading(false);
    }
  };


  return (
    <>
      <div style={{ textAlign: 'center', marginBottom: 24 }}>
        <Avatar 
          size={100} 
          src={avatarUrl} 
          icon={!avatarUrl && <UserOutlined />} 
        />
      </div>
      <Form
        form={form}
        initialValues={initialValues}
        onFinish={onFinish}
        layout="vertical"
      >
        <Form.Item
          name="name"
          label="姓名"
          rules={[{ required: true, message: '请输入姓名' }]}
        >
          <Input prefix={<UserOutlined />} placeholder="请输入姓名" />
        </Form.Item>

        <Form.Item
          name="email"
          label="邮箱"
          rules={[
            { required: true, message: '请输入邮箱' },
            { type: 'email', message: '请输入有效的邮箱地址' }
          ]}
        >
          <Input prefix={<MailOutlined />} placeholder="请输入邮箱" />
        </Form.Item>

        <Form.Item
          name="phone"
          label="手机号"
          rules={[
            { required: true, message: '请输入手机号' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号' }
          ]}
        >
          <Input prefix={<PhoneOutlined />} placeholder="请输入手机号" />
        </Form.Item>

        <Form.Item
          name="address"
          label="收货地址"
          rules={[{ required: true, message: '请输入收货地址' }]}
        >
          <Input.TextArea rows={4} placeholder="请输入详细地址" />
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading}>
            保存修改
          </Button>
        </Form.Item>
      </Form>
    </>
  )
}

export default UserProfileForm