import React, { useState, useEffect } from 'react'
import { Row, Col, message, Pagination } from 'antd'
import { useNavigate } from 'react-router-dom'
import BookSearch from '../../components/BookSearch.jsx'
import BookCard from '../../components/bookCard.jsx'
import useCart from '../../components/useCart.jsx'
import useSessionCheck from '../../hooks/useSessionCheck.jsx'
import BookService from '../../service/bookService.jsx'

const Home = () => {
  // 搜索框的值
  const [inputValue, setInputValue] = useState('')
  // 路由跳转
  const navigate = useNavigate()
  // 调用自定义hook 获取addToCart函数和消息提示组件
  const { addToCart, cartContextHolder } = useCart()
  // 使用session检查Hook进行页面级别的session验证
  useSessionCheck()
  const [books, setBooks] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTimeout, setSearchTimeout] = useState(null)
  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize] = useState(8)
  const [totalBooks, setTotalBooks] = useState(0)
  const [isSearching, setIsSearching] = useState(false)
  // 标签搜索相关状态
  const [selectedTag, setSelectedTag] = useState(null)
  const [searchMode, setSearchMode] = useState('keyword') // 'keyword' 或 'tag'
  
  // 初始加载第一页书籍
  useEffect(() => {
    fetchBooksByPage(1)
  }, [])

  // 监听输入值变化，实现搜索功能
  useEffect(() => {
    // 如果是标签搜索模式，不处理关键字搜索
    if (searchMode === 'tag') {
      return
    }
    
    // 清除之前的定时器
    if (searchTimeout) {
      clearTimeout(searchTimeout)
    }    // 如果输入为空，显示分页书籍
    if (!inputValue.trim()) {
      setIsSearching(false)
      fetchBooksByPage(currentPage)
      return
    }

    // 设置新的定时器，防止频繁请求
    const timeout = setTimeout(() => {
      setIsSearching(true)
      searchBooks(inputValue)
    }, 500) // 输入停止后500ms再搜索

    setSearchTimeout(timeout)

    // 组件卸载时清除定时器
    return () => {
      if (timeout) {
        clearTimeout(timeout)
      }
    }
  }, [inputValue, searchMode])
  // 分页获取书籍
  const fetchBooksByPage = async (page) => {
    console.log(`Fetching books for page: ${page}`)
    try {
      setLoading(true)
      const data = await BookService.getBooksByPage(page - 1, pageSize) // 后端页码从0开始
      setBooks(data.content)
      setTotalBooks(data.totalElements)
      setCurrentPage(page)
    } catch (error) {
      message.error('获取图书列表失败，请稍后再试')
      console.error('Failed to fetch books:', error)
    } finally {
      setLoading(false)
    }
  }

  // 获取所有书籍（保留用于搜索时的备用）
  const fetchAllBooks = async () => {
    console.log('Fetching all books...')
    try {
      setLoading(true)
      const data = await BookService.getAllBooks()
      setBooks(data)
      setTotalBooks(data.length)
    } catch (error) {
      message.error('获取图书列表失败，请稍后再试')
      console.error('Failed to fetch books:', error)
    } finally {
      setLoading(false)
    }
  }
  // 搜索书籍
  const searchBooks = async (keyword) => {
    console.log(`Searching books with keyword: ${keyword}`)
    try {
      setLoading(true)
      const data = await BookService.searchBooks(keyword)
      setBooks(data)
      setTotalBooks(data.length)
    } catch (error) {
      message.error('搜索图书失败，请稍后再试')
      console.error('Failed to search books:', error)
    } finally {
      setLoading(false)
    }
  }

  // 按标签搜索书籍（通过Neo4j关联标签）
  const searchBooksByTag = async (tag) => {
    console.log(`Searching books with tag: ${tag}`)
    try {
      setLoading(true)
      setIsSearching(true)
      const data = await BookService.searchByTag(tag)
      setBooks(data)
      setTotalBooks(data.length)
      message.success(`已找到 ${data.length} 本相关图书`)
    } catch (error) {
      message.error('按标签搜索图书失败，请稍后再试')
      console.error('Failed to search books by tag:', error)
    } finally {
      setLoading(false)
    }
  }

  // 处理搜索模式切换
  const handleSearchModeChange = (mode) => {
    setSearchMode(mode)
    // 切换模式时清空搜索状态
    if (mode === 'keyword') {
      setSelectedTag(null)
      if (!inputValue.trim()) {
        setIsSearching(false)
        fetchBooksByPage(1)
      }
    } else {
      setInputValue('')
      if (!selectedTag) {
        setIsSearching(false)
        fetchBooksByPage(1)
      }
    }
  }

  // 处理标签选择变化
  const handleTagChange = (tag) => {
    setSelectedTag(tag)
    if (!tag) {
      // 清除标签时，恢复分页显示
      setIsSearching(false)
      fetchBooksByPage(1)
    }
  }

  // 处理分页变化
  const handlePageChange = (page) => {
    if (!isSearching) {
      fetchBooksByPage(page)
    }
  }

  // 处理输入变化
  const handleInputChange = (e) => {
    setInputValue(e.target.value)
  }

  // 跳转到书籍详情页函数
  const goToBookDetail = (bookId) => {
    navigate(`/books/${bookId}`)
  }

  return (
    <div style={{ padding: 24 }}>
      {cartContextHolder}
      <BookSearch 
        value={inputValue}
        onChange={handleInputChange}
        onTagSearch={searchBooksByTag}
        selectedTag={selectedTag}
        onTagChange={handleTagChange}
        searchMode={searchMode}
        onSearchModeChange={handleSearchModeChange}
      />
      <Row gutter={[16, 16]}>
        {loading ? (
          <Col span={24} style={{ textAlign: 'center' }}>
            正在加载图书...
          </Col>
        ) : books.length === 0 ? (
          <Col span={24} style={{ textAlign: 'center' }}>
            未找到相关图书
          </Col>
        ) : (
          books.map(book => (
            <Col key={book.id} xs={24} sm={12} md={8} lg={6}>
              <div style={{ height: '100%' }}>  
                <BookCard 
                  book={book}
                  onAddToCart={addToCart}
                  onViewDetail={goToBookDetail}
                />
              </div>
            </Col>          ))
        )}
      </Row>
      
      {/* 分页组件 - 只在非搜索状态下显示 */}
      {!isSearching && !loading && books.length > 0 && (
        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <Pagination
            current={currentPage}
            total={totalBooks}
            pageSize={pageSize}
            onChange={handlePageChange}
            showSizeChanger={false}
            showQuickJumper
            showTotal={(total, range) => 
              `第 ${range[0]}-${range[1]} 条，共 ${total} 条`
            }
          />
        </div>
      )}
    </div>
  )
}

export default Home