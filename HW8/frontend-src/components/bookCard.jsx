import { Card, Button, Image, Typography, Tag } from 'antd'
import { ShoppingCartOutlined, TagsOutlined } from '@ant-design/icons'

const { Text } = Typography
// 书籍卡片组件
// 接收book、onAddToCart和onViewDetail三个属性，用于控制书籍数据、添加到购物车和查看详情的回调函数
const BookCard = ({ book, onAddToCart, onViewDetail }) => {
  return (
    <Card
      // 卡片的样式设置，包括高度、溢出隐藏、图片的样式等
      hoverable
      onClick={() => onViewDetail(book.id)}
      style={{ 
        height: '100%',  // 确保卡片占满父容器高度
        display: 'flex',
        flexDirection: 'column'
      }}
      cover={
        <div style={{ height: 200, overflow: 'hidden' }}>
          <Image
            alt={book.title}
            src={book.cover}
            style={{ 
              width: '100%',
              height: '100%',
              objectFit: 'cover',
              objectPosition: 'center'
            }}
            preview={true}
          />
        </div>
      }
      bodyStyle={{ 
        flex: '1 0 auto',  // 让内容区域伸展填充空间
        display: 'flex', 
        flexDirection: 'column',
        paddingBottom: 0  // 减少内部底部填充
      }}
      // 卡片的底部操作区域，包括一个按钮，用于添加到购物车
      actions={[
        <Button 
          type="primary" 
          icon={<ShoppingCartOutlined />}
          onClick={(e) => {
            e.stopPropagation()
            onAddToCart(book, e)
          }}
        >
          加入购物车
        </Button>
      ]}
    >
      {/* 卡片的元数据区域，包括标题和描述信息 */}
      <div style={{ flex: 1 }}>  {/* 让内容区域自动填充剩余空间 */}
        <Card.Meta
          title={
            <div style={{ 
              height: '44px',  // 固定标题区域高度
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              display: '-webkit-box',
              WebkitLineClamp: 2,
              WebkitBoxOrient: 'vertical'
            }}>
              {book.title}
            </div>
          }          description={
            <>
              <div style={{ 
                height: '22px',  // 固定作者区域高度
                overflow: 'hidden',
                whiteSpace: 'nowrap',
                textOverflow: 'ellipsis'
              }}>
                <Text type="secondary">{book.author}</Text>
              </div>
              {book.publisher && (
                <div style={{ 
                  height: '22px',  // 固定出版社区域高度
                  overflow: 'hidden',
                  whiteSpace: 'nowrap',
                  textOverflow: 'ellipsis',
                  marginTop: 4
                }}>
                  <Text type="secondary" style={{ fontSize: 12 }}>出版社: {book.publisher}</Text>
                </div>
              )}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 8 }}>
                <Text strong style={{ color: '#ff4d4f' }}>
                  ¥{book.price}
                </Text>
                {book.stock !== undefined && (
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    库存: {book.stock}
                  </Text>
                )}
              </div>
              {/* 标签展示 */}
              {book.tags && (
                <div style={{ 
                  marginTop: 8, 
                  height: '24px',
                  overflow: 'hidden'
                }}>
                  {book.tags.split(',').slice(0, 3).map((tag, index) => (
                    <Tag 
                      key={index} 
                      color="blue" 
                      style={{ fontSize: 10, padding: '0 4px', marginRight: 4 }}
                    >
                      {tag.trim()}
                    </Tag>
                  ))}
                </div>
              )}
            </>
          }
        />
      </div>
    </Card>
  )
}

export default BookCard