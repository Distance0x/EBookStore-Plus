import { Table, InputNumber, Space, Button, Tag, message, Tooltip } from 'antd'
import { DeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons'

const CartTable = ({ cartItems, onQuantityChange, onRemove, insufficientStockItems = [] }) => {
  
  // 处理数量变化的包装函数，添加客户端验证
  const handleQuantityChange = (bookId, newQuantity, record) => {
    if (newQuantity <= 0) {
      message.error('数量必须大于0');
      return;
    }
    
    if (record.stock !== undefined && newQuantity > record.stock) {
      message.error(`数量不能超过库存量(${record.stock})`);
      return;
    }
      onQuantityChange(bookId, newQuantity);
  };

  // 检查书籍是否库存不足
  const isInsufficientStock = (bookId) => {
    return insufficientStockItems.some(item => item.bookId === bookId);
  };
  
  const columns = [
    {
      title: '商品',
      dataIndex: 'name',
      key: 'name',
      render: (text, record) => (
        <Space>
          <img 
            src={record.cover} 
            alt={text}
            style={{ 
              width: 60, 
              height: 80, 
              objectFit: 'cover',
              opacity: record.deleted ? 0.5 : 1
            }}
          />          <div>
            <div style={{ 
              color: record.deleted ? '#999' : '#000',
              textDecoration: record.deleted ? 'line-through' : 'none'
            }}>
              {text}
              {isInsufficientStock(record.bookId) && (
                <Tooltip title="库存不足，请调整数量或删除此书籍">
                  <ExclamationCircleOutlined 
                    style={{ 
                      color: '#faad14', 
                      marginLeft: 8,
                      fontSize: '16px'
                    }} 
                  />
                </Tooltip>
              )}
            </div>
            {record.deleted && (
              <Tag color="red" size="small" style={{ marginTop: 4 }}>
                已删除
              </Tag>
            )}
            {isInsufficientStock(record.bookId) && !record.deleted && (
              <Tag color="orange" size="small" style={{ marginTop: 4 }}>
                库存不足
              </Tag>
            )}
          </div>
        </Space>
      ),
    },
    {
      title: '单价',
      dataIndex: 'price',
      key: 'price',
      render: price => `¥${price.toFixed(2)}`,
    },    {
      title: '数量',
      dataIndex: 'quantity',
      key: 'quantity',
      render: (quantity, record) => (
        <div>
          <InputNumber
            min={1}
            max={record.stock || 99}
            value={quantity}
            onChange={(value) => handleQuantityChange(record.key, value, record)}
            disabled={record.deleted}
            onStep={(value, info) => {
              // 处理步进器按钮点击
              if (info.type === 'up' || info.type === 'down') {
                handleQuantityChange(record.key, value, record);
              }
            }}
          />
          {record.stock !== undefined && !record.deleted && (
            <div style={{ fontSize: '12px', color: '#999', marginTop: '4px' }}>
              库存: {record.stock}
            </div>
          )}
        </div>
      ),
    },{
      title: '小计',
      key: 'subtotal',
      // render 方法用于计算每个商品的小计，使用toFixed(2)方法保留两位小数 
      // _ 表示第一个参数，即当前行的数据，record 表示当前行的所有数据
      
      render: (_, record) => (
        <span style={{ 
          color: record.deleted ? '#999' : '#000',
        //   textDecoration: record.deleted ? 'line-through' : 'none'
        }}>
          ¥{(record.price * record.quantity).toFixed(2)}
        </span>
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button 
          type="text" 
          danger 
          icon={<DeleteOutlined />}
          onClick={() => onRemove(record.key)}
        >
          删除
        </Button>
      ),
    },
  ]

  return (
    <Table 
      columns={columns} 
      dataSource={cartItems} 
      pagination={false}
      rowKey="key"
    />
  )
}

export default CartTable