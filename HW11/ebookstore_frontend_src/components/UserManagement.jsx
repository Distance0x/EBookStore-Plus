import { 
  Table, 
  Button, 
  Space, 
  message, 
  Tag,
  Typography,
  Switch,
  Avatar,
  Pagination
} from 'antd'
import { UserOutlined, MailOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import AdminService from '../service/AdminService.jsx'
import useAdminSessionCheck from '../hooks/useAdminSessionCheck.jsx'

const { Title } = Typography

const UserManagement = () => {
  // 使用管理员session检查hook
  useAdminSessionCheck();
  
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [currentUser, setCurrentUser] = useState(null)
  
  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize] = useState(10)
  const [totalRecords, setTotalRecords] = useState(0)

  useEffect(() => {
    fetchUsers()
    getCurrentUser()
  }, [])

  const getCurrentUser = () => {
    // 从sessionStorage获取当前用户信息
    const user = JSON.parse(sessionStorage.getItem('user') || '{}')
    setCurrentUser(user)
  }

  const fetchUsers = async (page = 1) => {
    try {
      setLoading(true)
      const response = await AdminService.getUsersByPage(page - 1, pageSize, 'id', 'asc')
      // 转换后端数据格式以匹配前端期望
      const transformedUsers = (response.content || []).map(user => ({
        ...user,
        enabled: user.status === 'active', // 转换status为enabled布尔值
        role: user.role === 'admin' ? 'ADMIN' : 'USER', // 转换role格式
        username: user.account // 添加username字段（使用account）
      }))
      setUsers(transformedUsers)
      setTotalRecords(response.totalElements || 0)
      setCurrentPage(page)
    } catch (error) {
      message.error('获取用户列表失败')
    } finally {
      setLoading(false)
    }
  }

  // 处理分页变化
  const handlePageChange = (page) => {
    fetchUsers(page)
  }
  const handleStatusChange = async (userId, enabled) => {
    // 检查是否尝试禁用自己
    if (!enabled && currentUser && currentUser.id === userId) {
      message.error('不能禁用自己的账户')
      return
    }

    try {
      await AdminService.updateUserStatus(userId, enabled ? 'ACTIVE' : 'DISABLED')
      message.success(`用户已${enabled ? '启用' : '禁用'}`)
      fetchUsers()
    } catch (error) {
      message.error('更新用户状态失败')
    }
  }

  const columns = [
    {
      title: '头像',
      dataIndex: 'avatar',
      key: 'avatar',
      width: 80,
      render: (avatar, record) => (
        <Avatar
          size={40}
          src={avatar}
          icon={<UserOutlined />}
        />
      )
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 120
    },
    {
      title: '姓名',
      dataIndex: 'name',
      key: 'name',
      width: 100
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 200,
      render: (email) => (
        <Space>
          <MailOutlined />
          {email}
        </Space>
      )
    },
    {
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      width: 100,
      render: (role) => (
        <Tag color={role === 'ADMIN' ? 'red' : 'blue'}>
          {role === 'ADMIN' ? '管理员' : '普通用户'}
        </Tag>
      )
    },
    {
      title: '账户余额',
      dataIndex: 'balance',
      key: 'balance',
      width: 120,
      render: (balance) => `¥${balance ? balance.toLocaleString() : 0}`
    },
    {
      title: '状态',
      dataIndex: 'enabled',
      key: 'enabled',
      width: 100,
      render: (enabled) => (
        <Tag color={enabled ? 'green' : 'red'}>
          {enabled ? '启用' : '禁用'}
        </Tag>
      )
    },    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => {
        const isCurrentUser = currentUser && currentUser.id === record.id
        const isAdmin = record.role === 'ADMIN'
        
        return (
          <Space size="middle">
            {!isAdmin ? (
              <Switch
                checkedChildren="启用"
                unCheckedChildren="禁用"
                checked={record.enabled}
                onChange={(checked) => handleStatusChange(record.id, checked)}
              />
            ) : isCurrentUser ? (
              <Tag color="gold">当前管理员</Tag>
            ) : (
              <Tag color="red">管理员</Tag>
            )}
          </Space>
        )
      }
    }
  ]

  return (
    <div>
      <Title level={2}>用户管理</Title>
        <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        loading={loading}
        scroll={{ x: 1000 }}
        pagination={false}
      />

      {/* 自定义分页组件 */}
      <div style={{ marginTop: 16, textAlign: 'right' }}>
        <Pagination
          current={currentPage}
          pageSize={pageSize}
          total={totalRecords}
          showSizeChanger={false}
          showQuickJumper={true}
          showTotal={(total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条记录`}
          onChange={handlePageChange}
        />
      </div>
    </div>
  )
}

export default UserManagement
