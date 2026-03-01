import { 
  Table, 
  Tag, 
  Typography,
  Space,
  message,
  Collapse,
  List,
  Image,
  Pagination
} from 'antd'
import { ShoppingCartOutlined, UserOutlined, CalendarOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import AdminService from '../service/AdminService.jsx'
import OrderSearch from './orderSearch.jsx'
import useAdminSessionCheck from '../hooks/useAdminSessionCheck.jsx'

const { Title } = Typography
const { Panel } = Collapse

const OrderManagement = () => {
  // 使用管理员session检查hook
  useAdminSessionCheck();
  
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(false)
  const [searchLoading, setSearchLoading] = useState(false)
  
  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize] = useState(10)
  const [totalOrders, setTotalOrders] = useState(0)
  const [isSearchMode, setIsSearchMode] = useState(false)
  const [searchParams, setSearchParams] = useState({})

  useEffect(() => {
    fetchOrdersByPage(1)
  }, [])
  
  // 分页获取订单
  const fetchOrdersByPage = async (page) => {
    try {
      setLoading(true)
      const response = await AdminService.getAllOrdersByPage(page - 1, pageSize)
      // 转换后端数据格式以匹配前端期望
      const transformedOrders = response.content.map(order => ({
        ...order,
        totalPrice: order.total, // 后端返回的是 total，前端期望 totalPrice
        createdAt: order.createTime || order.Time, // 后端返回 createTime，前端期望 createdAt
        paidAt: order.paymentTime, // 后端返回 paymentTime，前端期望 paidAt
        // 转换订单项数据结构
        orderItems: order.orderItems?.map(item => ({
          ...item,
          book: {
            id: item.bookId,
            title: item.bookTitle,
            author: item.bookAuthor,
            cover: item.bookCover
          }
        })) || []
      }))
      setOrders(transformedOrders)
      setTotalOrders(response.totalElements)
      setCurrentPage(page)
    } catch (error) {
      message.error('获取订单列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchOrders = async () => {
    try {
      setLoading(true)
      const response = await AdminService.getAllOrders()
      // 转换后端数据格式以匹配前端期望
      const transformedOrders = response.map(order => ({
        ...order,
        totalPrice: order.total, // 后端返回的是 total，前端期望 totalPrice
        createdAt: order.createTime || order.Time, // 后端返回 createTime，前端期望 createdAt
        paidAt: order.paymentTime, // 后端返回 paymentTime，前端期望 paidAt
        // 转换订单项数据结构
        orderItems: order.orderItems?.map(item => ({
          ...item,
          book: {
            id: item.bookId,
            title: item.bookTitle,
            author: item.bookAuthor,
            cover: item.bookCover
          }
        })) || []
      }))
      setOrders(transformedOrders)
    } catch (error) {
      message.error('获取订单列表失败')
    } finally {
      setLoading(false)
    }
  }
  // 搜索订单（分页）
  const searchOrdersByPage = async (searchParams, page = 1) => {
    setSearchLoading(true);
    try {
      let response;
      
      // 如果没有搜索条件，加载分页订单
      if (!searchParams.bookTitle && !searchParams.startTime && !searchParams.endTime) {
        setIsSearchMode(false);
        response = await AdminService.getAllOrdersByPage(page - 1, pageSize);
      } else {
        // 有搜索条件，使用搜索API（分页）
        setIsSearchMode(true);
        setSearchParams(searchParams);
        response = await AdminService.searchAllOrdersByPage(
          searchParams.bookTitle,
          searchParams.startTime,
          searchParams.endTime,
          page - 1,
          pageSize
        );
      }
      
      // 转换后端数据格式以匹配前端期望
      const transformedOrders = response.content.map(order => ({
        ...order,
        totalPrice: order.total, // 后端返回的是 total，前端期望 totalPrice
        createdAt: order.createTime || order.Time, // 后端返回 createTime，前端期望 createdAt
        paidAt: order.paymentTime, // 后端返回 paymentTime，前端期望 paidAt
        // 转换订单项数据结构
        orderItems: order.orderItems?.map(item => ({
          ...item,
          book: {
            id: item.bookId,
            title: item.bookTitle,
            author: item.bookAuthor,
            cover: item.bookCover
          }
        })) || []
      }))
      
      setOrders(transformedOrders);
      setTotalOrders(response.totalElements);
      setCurrentPage(page);
      
      if (searchParams.bookTitle || searchParams.startTime || searchParams.endTime) {
        message.success(`找到 ${response.totalElements} 个符合条件的订单`);
      }
    } catch (error) {
      console.error('搜索订单失败:', error);
      message.error('搜索订单失败: ' + (error.message || '未知错误'));
    } finally {
      setSearchLoading(false);
    }
  };

  // 处理管理员订单搜索
  const handleSearch = async (searchParams) => {
    console.log('管理员搜索参数:', searchParams);
    searchOrdersByPage(searchParams, 1); // 搜索时重置到第1页
  };

  // 处理分页变化
  const handlePageChange = (page) => {
    if (isSearchMode) {
      searchOrdersByPage(searchParams, page);
    } else {
      fetchOrdersByPage(page);
    }
  };

  // 处理管理员订单搜索（保留用于兼容，但标记为废弃）
  const handleSearchOld = async (searchParams) => {
    console.log('管理员搜索参数:', searchParams);
    setSearchLoading(true);
    try {
      let response;
      
      // 如果没有搜索条件，加载所有订单
      if (!searchParams.bookTitle && !searchParams.startTime && !searchParams.endTime) {
        response = await AdminService.getAllOrders();
      } else {
        // 有搜索条件，使用搜索API
        response = await AdminService.searchAllOrders(
          searchParams.bookTitle,
          searchParams.startTime,
          searchParams.endTime
        );
      }
      
      // 转换后端数据格式以匹配前端期望
      const transformedOrders = response.map(order => ({
        ...order,
        totalPrice: order.total, // 后端返回的是 total，前端期望 totalPrice
        createdAt: order.createTime || order.Time, // 后端返回 createTime，前端期望 createdAt
        paidAt: order.paymentTime, // 后端返回 paymentTime，前端期望 paidAt
        // 转换订单项数据结构
        orderItems: order.orderItems?.map(item => ({
          ...item,
          book: {
            id: item.bookId,
            title: item.bookTitle,
            author: item.bookAuthor,
            cover: item.bookCover
          }
        })) || []
      }))
      
      setOrders(transformedOrders);
      
      if (searchParams.bookTitle || searchParams.startTime || searchParams.endTime) {
        message.success(`找到 ${transformedOrders.length} 个符合条件的订单`);
      }
    } catch (error) {
      console.error('搜索订单失败:', error);
      message.error('搜索订单失败: ' + (error.message || '未知错误'));
    } finally {
      setSearchLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return 'orange'
      case 'PAID':
        return 'green'
      case 'CANCELLED':
        return 'red'
      default:
        return 'default'
    }
  }

  const getStatusText = (status) => {
    switch (status) {
      case 'PENDING':
        return '待支付'
      case 'PAID':
        return '已支付'
      case 'CANCELLED':
        return '已取消'
      default:
        return status
    }
  }
  const expandedRowRender = (record) => {
    return (
      <div style={{ margin: 0 }}>
        <Title level={5}>订单详情</Title>
        <List
          itemLayout="horizontal"
          dataSource={record.orderItems || []}
          renderItem={item => (
            <List.Item>
              <List.Item.Meta
                avatar={
                  <Image
                    width={50}
                    height={70}
                    src={item.book?.cover || item.bookCover}
                    fallback="/src/assets/image/default-book.jpg"
                    style={{ objectFit: 'cover' }}
                  />
                }
                title={item.book?.title || item.bookTitle || '未知书籍'}
                description={
                  <Space direction="vertical" size={0}>
                    <span>作者: {item.book?.author || item.bookAuthor || '-'}</span>
                    <span>单价: ¥{item.price ? Number(item.price).toFixed(2) : '0.00'}</span>
                    <span>数量: {item.quantity}</span>
                  </Space>
                }
              />
              <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                ¥{item.subtotal ? Number(item.subtotal).toFixed(2) : 
                   (Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2)}
              </div>
            </List.Item>
          )}
        />
        <div style={{ marginTop: '16px', textAlign: 'right', fontSize: '16px', fontWeight: 'bold' }}>
          订单总金额: ¥{record.totalPrice ? Number(record.totalPrice).toFixed(2) : '0.00'}
        </div>
      </div>
    )
  }

  const columns = [
    {
      title: '订单ID',
      dataIndex: 'id',
      key: 'id',
      width: 80
    },
    {
      title: '用户',
      dataIndex: 'user',
      key: 'user',
      width: 120,
      render: (user) => (
        <Space>
          <UserOutlined />
          {user?.name || user?.username || '未知用户'}
        </Space>
      )
    },    {
      title: '订单金额',
      dataIndex: 'totalPrice',
      key: 'totalPrice',
      width: 100,
      render: (price) => `¥${price ? Number(price).toFixed(2) : '0.00'}`,
      sorter: (a, b) => Number(a.totalPrice || 0) - Number(b.totalPrice || 0)
    },
    {
      title: '商品数量',
      dataIndex: 'orderItems',
      key: 'itemCount',
      width: 100,
      render: (items) => items ? items.length : 0
    },
    {
      title: '订单状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => (
        <Tag color={getStatusColor(status)}>
          {getStatusText(status)}
        </Tag>
      ),
      filters: [
        { text: '待支付', value: 'PENDING' },
        { text: '已支付', value: 'PAID' },
        { text: '已取消', value: 'CANCELLED' }
      ],
      onFilter: (value, record) => record.status === value
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 150,
      render: (date) => (
        <Space>
          <CalendarOutlined />
          {date ? new Date(date).toLocaleString() : '-'}
        </Space>
      ),
      sorter: (a, b) => new Date(a.createdAt) - new Date(b.createdAt)
    },
    {
      title: '支付时间',
      dataIndex: 'paidAt',
      key: 'paidAt',
      width: 150,
      render: (date) => date ? (
        <Space>
          <CalendarOutlined />
          {new Date(date).toLocaleString()}
        </Space>
      ) : '-'
    }
  ]
  return (
    <div>
      <Title level={2}>订单管理</Title>      <OrderSearch 
        onSearch={handleSearch} 
        loading={searchLoading}
      />
      <Table
        columns={columns}
        dataSource={orders}
        rowKey="id"
        loading={loading}
        expandable={{
          expandedRowRender,
          expandRowByClick: true,
          rowExpandable: (record) => record.orderItems && record.orderItems.length > 0
        }}
        scroll={{ x: 800 }}
        pagination={false} // 禁用内置分页
      />
      
      {/* 自定义分页组件 */}
      {!loading && orders.length > 0 && (
        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <Pagination
            current={currentPage}
            total={totalOrders}
            pageSize={pageSize}
            onChange={handlePageChange}
            showSizeChanger={false}
            showQuickJumper
            showTotal={(total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 个订单`
            }
          />
        </div>
      )}
    </div>
  )
}

export default OrderManagement
