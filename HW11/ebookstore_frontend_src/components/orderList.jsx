import { Table, Button, Modal, Tag, Space, List, Image, Typography } from 'antd'
import { CheckOutlined, ExclamationCircleOutlined, EyeOutlined, CloseOutlined, EditOutlined } from '@ant-design/icons'

const { Title } = Typography

const OrderList = ({ 
  orders,
  onPay,
  onCancel, // 新增：取消订单的回调函数
  onEditContact, // 新增：修改联系信息的回调函数
  onDeleteAll,
  currentOrderId,
  isPayModalOpen,
  isDeleteModalOpen,
  isCancelModalOpen, // 新增：取消订单模态框状态
  onPayOk,
  onCancelPay,
  onCancelOk, // 新增：确认取消订单回调
  onCancelCancel, // 新增：取消取消订单回调
  onDeleteOk,
  onCancelDelete,
  loading,
  onViewDetails // 新增：查看详情的回调函数
}) => {
  // 展开行渲染函数 - 显示订单详情
  const expandedRowRender = (record) => {
    if (!record.orderItems || record.orderItems.length === 0) {
      return (
        <div style={{ padding: '16px', textAlign: 'center', color: '#999' }}>
          暂无订单详情
        </div>
      )
    }

    return (
      <div style={{ margin: 0, backgroundColor: '#fafafa', padding: '16px' }}>
        <Title level={5}>订单详情</Title>
        <List
          itemLayout="horizontal"
          dataSource={record.orderItems}
          renderItem={item => (
            <List.Item>
              <List.Item.Meta
                avatar={
                  <Image
                    width={50}
                    height={70}
                    src={item.bookCover}
                    fallback="/src/assets/image/default-book.jpg"
                    style={{ objectFit: 'cover' }}
                  />
                }
                title={item.bookTitle || '未知书籍'}
                description={
                  <Space direction="vertical" size={0}>
                    <span>作者: {item.bookAuthor || '-'}</span>
                    <span>单价: ¥{item.price ? Number(item.price).toFixed(2) : '0.00'}</span>
                    <span>数量: {item.quantity}</span>
                  </Space>
                }
              />
              <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                小计: ¥{(Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2)}
              </div>
            </List.Item>
          )}
        />
        <div style={{ marginTop: '16px', textAlign: 'right', fontSize: '16px', fontWeight: 'bold' }}>
          订单总金额: ¥{record.total ? Number(record.total).toFixed(2) : '0.00'}
        </div>
      </div>
    )
  }

  const columns = [
    {
      title: '订单编号',
      dataIndex: 'orderNumber',
      key: 'orderNumber',
    },
    {
      title: '总金额',
      dataIndex: 'total',
      key: 'total',
      render: total => `¥${total}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: status => (
        <Tag color={status === '已完成' ? 'green' : 'orange'}>
          {status}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'Time',
      key: 'Time',
    },
    {
      title: '收货地址',
      dataIndex: 'address',
      key: 'address',
    },
    {
      title: '联系电话',
      dataIndex: 'phone',
      key: 'phone',
    },    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button 
            type="primary" 
            icon={<CheckOutlined />}
            onClick={(e) => {
              e.stopPropagation(); // 阻止事件冒泡
              onPay(record.id);
            }}
            disabled={record.status === '已完成' || record.status === '已取消'}
          >
            {record.status === '已完成' ? '已完成' : 
             record.status === '已取消' ? '已取消' : '立即支付'}
          </Button>
          {(record.status === '待支付' || record.status === 'PENDING') && (
            <>
              <Button 
                icon={<EditOutlined />}
                onClick={(e) => {
                  e.stopPropagation(); // 阻止事件冒泡
                  onEditContact && onEditContact(record);
                }}
                title="修改联系信息"
              >
                修改联系信息
              </Button>
              <Button 
                danger
                icon={<CloseOutlined />}
                onClick={(e) => {
                  e.stopPropagation(); // 阻止事件冒泡
                  onCancel && onCancel(record.id);
                }}
              >
                取消订单
              </Button>
            </>
          )}
        </Space>
      ),
    }
  ]

  return (
    <>
      <Table 
        columns={columns} 
        dataSource={orders} 
        rowKey="id"
        loading={loading}
        expandable={{
          expandedRowRender,
          expandRowByClick: true,
          rowExpandable: (record) => true // 所有订单都可以展开查看详情
        }}
        footer={() => (
          <div style={{ textAlign: 'right' }}>
            {/* <Button 
              type="primary" 
              danger
              onClick={onDeleteAll}
              disabled={orders.length === 0}
            >
              删除所有订单
            </Button> */}
          </div>
        )}
      />
      
      <Modal 
        title="支付确认" 
        open={isPayModalOpen} 
        onOk={onPayOk} 
        onCancel={onCancelPay} 
        okText="确认支付"
        cancelText="取消"
      >
        <p>您确定要支付订单 {currentOrderId} 吗？</p>
      </Modal>      <Modal
        title="确认删除"
        open={isDeleteModalOpen}
        onOk={onDeleteOk}
        onCancel={onCancelDelete}
        okText="确认"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p><ExclamationCircleOutlined style={{ color: 'red', marginRight: 8 }} />确定要删除所有订单吗？此操作不可恢复。</p>
      </Modal>

      <Modal
        title="取消订单确认"
        open={isCancelModalOpen}
        onOk={onCancelOk}
        onCancel={onCancelCancel}
        okText="确认取消"
        cancelText="我再想想"
        okButtonProps={{ danger: true }}
      >
        <p><ExclamationCircleOutlined style={{ color: 'orange', marginRight: 8 }} />确定要取消订单 {currentOrderId} 吗？</p>
      </Modal>
    </>
  )
}

export default OrderList