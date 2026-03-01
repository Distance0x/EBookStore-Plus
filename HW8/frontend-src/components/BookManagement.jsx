import { 
  Table, 
  Button, 
  Modal, 
  Form, 
  Input, 
  InputNumber, 
  Space, 
  message, 
  Popconfirm,
  Typography,
  Image,
  Card,
  Row,
  Col,
  Pagination,
  Select,
  Tag
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined, ClearOutlined, TagsOutlined } from '@ant-design/icons'
import { useState, useEffect } from 'react'
import AdminService from '../service/AdminService.jsx'
import BookService from '../service/bookService.jsx'
import useAdminSessionCheck from '../hooks/useAdminSessionCheck.jsx'

const { Title } = Typography
const { TextArea } = Input

const BookManagement = () => {
  // 使用管理员session检查hook
  useAdminSessionCheck();
  
  const [books, setBooks] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingBook, setEditingBook] = useState(null)
  const [form] = Form.useForm()
  const [searchKeyword, setSearchKeyword] = useState('')
  const [searchLoading, setSearchLoading] = useState(false)
  
  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize] = useState(10)
  const [totalRecords, setTotalRecords] = useState(0)
  
  // 标签相关状态
  const [allTags, setAllTags] = useState([])
  const [tagsLoading, setTagsLoading] = useState(false)

  useEffect(() => {
    fetchBooks()
    fetchAllTags()
  }, [])
  
  // 获取所有标签
  const fetchAllTags = async () => {
    try {
      setTagsLoading(true)
      const tags = await BookService.getAllTags()
      setAllTags(tags || [])
    } catch (error) {
      console.error('获取标签列表失败:', error)
    } finally {
      setTagsLoading(false)
    }
  }
  
  const fetchBooks = async (page = 1) => {
    try {
      setLoading(true)
      const response = await BookService.getBooksByPage(page - 1, pageSize, 'id', 'asc')
      setBooks(response.content || [])
      setTotalRecords(response.totalElements || 0)
      setCurrentPage(page)
    } catch (error) {
      message.error('获取书籍列表失败')
    } finally {
      setLoading(false)
    }
  }

  // 处理分页变化
  const handlePageChange = (page) => {
    if (searchKeyword) {
      searchBooks(searchKeyword, page)
    } else {
      fetchBooks(page)
    }
  }
  // 搜索书籍
  const searchBooks = async (keyword, page = 1) => {
    try {
      setSearchLoading(true)
      if (keyword) {
        // 搜索时暂时使用非分页API，因为后端还没有搜索分页API
        const response = await BookService.searchBooks(keyword)
        setBooks(response)
        setTotalRecords(response.length)
        setCurrentPage(1)
        message.success(`找到 ${response.length} 本相关书籍`)
      } else {
        // 如果关键词为空，回到正常分页
        fetchBooks(1)
      }
    } catch (error) {
      message.error('搜索书籍失败')
    } finally {
      setSearchLoading(false)
    }
  }

  // 处理搜索
  const handleSearch = () => {
    if (searchKeyword.trim()) {
      searchBooks(searchKeyword.trim())
    } else {
      handleReset()
    }
  }

  // 处理重置
  const handleReset = () => {
    setSearchKeyword('')
    fetchBooks()
  }

  // 处理搜索输入变化
  const handleSearchChange = (e) => {
    setSearchKeyword(e.target.value)
  }

  const handleAdd = () => {
    setEditingBook(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (book) => {
    setEditingBook(book)
    // 处理标签：将逗号分隔的字符串转换为数组
    const bookData = {
      ...book,
      tags: book.tags ? book.tags.split(',').map(tag => tag.trim()) : []
    }
    form.setFieldsValue(bookData)
    setModalVisible(true)
  }
  const handleDelete = async (bookId) => {
    try {
      await BookService.deleteBook(bookId)
      message.success('图书已删除')
      handleSubmitAfterUpdate()
    } catch (error) {
      message.error('删除失败')
    }
  }
  const handleSubmit = async (values) => {
    try {
      // 处理标签：将数组转换为逗号分隔的字符串
      const submitData = {
        ...values,
        tags: Array.isArray(values.tags) ? values.tags.join(',') : values.tags || ''
      }
      
      if (editingBook) {
        await AdminService.updateBook(editingBook.id, submitData)
        message.success('更新成功')
      } else {
        await AdminService.createBook(submitData)
        message.success('添加成功')
      }
      setModalVisible(false)
      handleSubmitAfterUpdate()
    } catch (error) {
      message.error(editingBook ? '更新失败' : '添加失败')
    }
  }

  const handleSubmitAfterUpdate = async () => {
    // 更新或添加后，根据当前搜索状态重新加载数据
    if (searchKeyword.trim()) {
      await searchBooks(searchKeyword.trim())
    } else {
      await fetchBooks()
    }
  }

  const columns = [
    {
      title: '封面',
      dataIndex: 'cover',
      key: 'cover',
      width: 80,
      render: (cover) => (
        <Image
          width={50}
          height={70}
          src={cover}
          fallback="/src/assets/image/default-book.jpg"
          style={{ objectFit: 'cover' }}
        />
      )
    },
    {
      title: '书名',
      dataIndex: 'title',
      key: 'title',
      width: 200
    },
    {
      title: '作者',
      dataIndex: 'author',
      key: 'author',
      width: 120
    },
    {
      title: 'ISBN',
      dataIndex: 'isbn',
      key: 'isbn',
      width: 150
    },
    {
      title: '出版社',
      dataIndex: 'publisher',
      key: 'publisher',
      width: 120
    },
    {
      title: '价格',
      dataIndex: 'price',
      key: 'price',
      width: 80,
      render: (price) => `¥${price}`
    },    {
      title: '库存',
      dataIndex: 'stock',
      key: 'stock',
      width: 80
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      width: 200,
      render: (tags) => (
        tags ? (
          <Space size={[0, 4]} wrap>
            {tags.split(',').slice(0, 3).map((tag, index) => (
              <Tag key={index} color="blue" style={{ fontSize: 11 }}>
                {tag.trim()}
              </Tag>
            ))}
            {tags.split(',').length > 3 && <Tag>...</Tag>}
          </Space>
        ) : (
          <span style={{ color: '#999' }}>无</span>
        )
      )
    },
    {
      title: '状态',
      dataIndex: 'deleted',
      key: 'status',
      width: 80,
      render: (deleted) => (
        <span style={{ 
          color: deleted ? '#ff4d4f' : '#52c41a',
          fontWeight: 'bold'
        }}>
          {deleted ? '已删除' : '正常'}
        </span>
      )
    },    
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="middle">
          <Button
            type="primary"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            disabled={record.deleted}
          >
            编辑
          </Button>
          {!record.deleted && (
            <Popconfirm
              title="确定要删除这本书吗？"
              description="删除后用户将无法看到此书，但购物车和订单中仍可显示"
              onConfirm={() => handleDelete(record.id)}
              okText="确定"
              cancelText="取消"
            >
              <Button
                type="primary"
                danger
                size="small"
                icon={<DeleteOutlined />}
              >
                删除
              </Button>
            </Popconfirm>
          )}
        </Space>
      )
    }
  ]
  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={2}>书籍管理</Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleAdd}
        >
          添加书籍
        </Button>
      </div>

      {/* 搜索区域 */}
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col flex="auto">
            <Input
              placeholder="请输入书名或作者进行搜索"
              value={searchKeyword}
              onChange={handleSearchChange}
              onPressEnter={handleSearch}
              allowClear
            />
          </Col>
          <Col>
            <Space>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={handleSearch}
                loading={searchLoading}
              >
                搜索
              </Button>
              <Button
                icon={<ClearOutlined />}
                onClick={handleReset}
              >
                重置
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>      <Table
        columns={columns}
        dataSource={books}
        rowKey="id"
        loading={loading}
        scroll={{ x: 800 }}
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

      <Modal
        title={editingBook ? '编辑书籍' : '添加书籍'}
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="title"
            label="书名"
            rules={[{ required: true, message: '请输入书名' }]}
          >
            <Input placeholder="请输入书名" />
          </Form.Item>

          <Form.Item
            name="author"
            label="作者"
            rules={[{ required: true, message: '请输入作者' }]}
          >
            <Input placeholder="请输入作者" />
          </Form.Item>

          <Form.Item
            name="isbn"
            label="ISBN"
            rules={[{ required: true, message: '请输入ISBN' }]}
          >
            <Input placeholder="请输入ISBN" />
          </Form.Item>

          <Form.Item
            name="publisher"
            label="出版社"
            rules={[{ required: true, message: '请输入出版社' }]}
          >
            <Input placeholder="请输入出版社" />
          </Form.Item>

          <Form.Item
            name="price"
            label="价格"
            rules={[{ required: true, message: '请输入价格' }]}
          >
            <InputNumber
              placeholder="请输入价格"
              min={0}
              step={0.01}
              precision={2}
              style={{ width: '100%' }}
              addonAfter="¥"
            />
          </Form.Item>

          <Form.Item
            name="stock"
            label="库存"
            rules={[{ required: true, message: '请输入库存数量' }]}
          >
            <InputNumber
              placeholder="请输入库存数量"
              min={0}
              style={{ width: '100%' }}
            />
          </Form.Item>

          <Form.Item
            name="cover"
            label="封面图片URL"
            rules={[{ required: true, message: '请输入封面图片URL' }]}
          >
            <Input placeholder="请输入封面图片URL" />
          </Form.Item>

          <Form.Item
            name="description"
            label="书籍描述"
          >
            <TextArea
              rows={4}
              placeholder="请输入书籍描述"
            />
          </Form.Item>

          <Form.Item
            name="tags"
            label="标签"
            tooltip="可以选择已有标签或输入新标签，多个标签用回车分隔"
          >
            <Select
              mode="tags"
              style={{ width: '100%' }}
              placeholder="选择或输入标签"
              loading={tagsLoading}
              suffixIcon={<TagsOutlined />}
              tokenSeparators={[',']}
              maxTagCount="responsive"
            >
              {allTags.map(tag => (
                <Select.Option key={tag} value={tag}>
                  {tag}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item style={{ textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setModalVisible(false)}>
                取消
              </Button>
              <Button type="primary" htmlType="submit">
                {editingBook ? '更新' : '添加'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default BookManagement
