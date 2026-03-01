import { Card, Button, Typography, Image, Space, Descriptions, Tag } from 'antd'
import { ShoppingCartOutlined, TagsOutlined } from '@ant-design/icons'

const { Title, Text } = Typography

const BookDetail = ({ book, onAddToCart, onBack }) => {
  return (
    <Card>
      <div style={{ display: 'flex', gap: 24 }}>
        <Image
          width={300}
          src={book.cover}
          alt={book.title}
          style={{ borderRadius: 8 }}        />
        <div style={{ flex: 1 }}>
          <Title level={2}>{book.title}</Title>
          
          <Descriptions column={1} size="small" style={{ marginBottom: 16 }}>
            <Descriptions.Item label="作者">{book.author}</Descriptions.Item>
            {book.publisher && (
              <Descriptions.Item label="出版社">{book.publisher}</Descriptions.Item>
            )}
            {book.isbn && (
              <Descriptions.Item label="ISBN">{book.isbn}</Descriptions.Item>
            )}
            {book.stock !== undefined && (
              <Descriptions.Item label="库存">
                <span style={{ color: book.stock > 0 ? '#52c41a' : '#ff4d4f' }}>
                  {book.stock} 册
                </span>
              </Descriptions.Item>
            )}
            {book.tags && (
              <Descriptions.Item label="标签">
                <Space size={[0, 4]} wrap>
                  {book.tags.split(',').map((tag, index) => (
                    <Tag key={index} color="blue" icon={<TagsOutlined />}>
                      {tag.trim()}
                    </Tag>
                  ))}
                </Space>
              </Descriptions.Item>
            )}
          </Descriptions>
          
          <Text strong style={{ fontSize: 24, color: '#ff4d4f', display: 'block', marginBottom: 16 }}>
            ¥{book.price}
          </Text>
          
          {book.description && (
            <div style={{ marginBottom: 24 }}>
              <Text strong style={{ display: 'block', marginBottom: 8 }}>内容简介:</Text>
              <Text>{book.description}</Text>
            </div>
          )}
          
          <Space direction="" style={{ width: '100%' }}>
            <Button 
              type="primary" 
              size="large"
              onClick={onAddToCart}
              icon={<ShoppingCartOutlined />}
              disabled={book.stock === 0}
            >
              {book.stock === 0 ? '缺货' : '加入购物车'}
            </Button>
            <Button size="large" onClick={onBack}>
              返回
            </Button>
          </Space>
        </div>
      </div>
    </Card>
  )
}

export default BookDetail