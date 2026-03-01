import React, { useState } from 'react';
import { Card, Input, Button, Typography, Space, Tag, List, message } from 'antd';
import { SearchOutlined, BookOutlined, UserOutlined } from '@ant-design/icons';
import AuthorService from '../../service/authorService';

const { Title, Text } = Typography;

const AuthorSearch = () => {
  const [searchTitle, setSearchTitle] = useState('');
  const [exactMatch, setExactMatch] = useState(false);
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  // 处理搜索
  const handleSearch = async () => {
    if (!searchTitle.trim()) {
      message.warning('请输入书名');
      return;
    }

    setLoading(true);
    try {
      const data = await AuthorService.getAuthorByTitle(searchTitle, exactMatch);
      setResult(data);
      
      if (data.found) {
        message.success(`找到 ${data.count} 本相关书籍`);
      } else {
        message.info('未找到相关书籍');
      }
    } catch (error) {
      message.error('查询失败，请稍后再试');
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  // 按Enter键搜索
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  return (
    <div style={{ 
      padding: '40px 20px', 
      maxWidth: '800px', 
      margin: '0 auto',
      minHeight: '80vh'
    }}>
      <Card 
        bordered={false}
        style={{ 
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
          borderRadius: '8px'
        }}
      >
        <Title level={2} style={{ textAlign: 'center', marginBottom: 30 }}>
          <BookOutlined /> 作者查询
        </Title>

        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          {/* 搜索输入框 */}
          <Space.Compact style={{ width: '100%' }}>
            <Input
              size="large"
              placeholder="请输入书名查询作者..."
              value={searchTitle}
              onChange={(e) => setSearchTitle(e.target.value)}
              onKeyPress={handleKeyPress}
              prefix={<SearchOutlined />}
              style={{ borderRadius: '4px 0 0 4px' }}
            />
            <Button
              type="primary"
              size="large"
              loading={loading}
              onClick={handleSearch}
              icon={<SearchOutlined />}
              style={{ borderRadius: '0 4px 4px 0' }}
            >
              查询
            </Button>
          </Space.Compact>

          {/* 匹配模式切换 */}
          <div style={{ textAlign: 'center' }}>
            <Space>
              <Text>匹配模式：</Text>
              <Tag 
                color={!exactMatch ? 'blue' : 'default'}
                style={{ cursor: 'pointer' }}
                onClick={() => setExactMatch(false)}
              >
                模糊匹配
              </Tag>
              <Tag 
                color={exactMatch ? 'blue' : 'default'}
                style={{ cursor: 'pointer' }}
                onClick={() => setExactMatch(true)}
              >
                精确匹配
              </Tag>
            </Space>
          </div>

          {/* 搜索结果 */}
          {result && (
            <Card 
              type="inner" 
              title={
                <Space>
                  <UserOutlined />
                  <Text>查询结果</Text>
                </Space>
              }
              style={{ marginTop: 20 }}
            >
              <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                {/* 作者信息 */}
                <div>
                  <Text strong>搜索书名：</Text>
                  <Text style={{ marginLeft: 10 }}>{result.searchTitle}</Text>
                </div>
                
                <div>
                  <Text strong>作者：</Text>
                  <Text 
                    style={{ 
                      marginLeft: 10, 
                      fontSize: '18px',
                      color: result.found ? '#1890ff' : '#999'
                    }}
                  >
                    {result.author}
                  </Text>
                </div>

                <div>
                  <Text strong>状态：</Text>
                  <Tag color={result.found ? 'success' : 'default'} style={{ marginLeft: 10 }}>
                    {result.found ? `找到 ${result.count} 本书` : '未找到'}
                  </Tag>
                </div>

                {/* 书籍列表 */}
                {result.found && result.books && result.books.length > 0 && (
                  <div style={{ marginTop: 20 }}>
                    <Text strong style={{ fontSize: '16px', marginBottom: 10, display: 'block' }}>
                      相关书籍列表：
                    </Text>
                    <List
                      size="small"
                      bordered
                      dataSource={result.books}
                      renderItem={(book, index) => (
                        <List.Item>
                          <Space>
                            <Tag color="blue">{index + 1}</Tag>
                            <Text strong>{book.title}</Text>
                            <Text type="secondary">- {book.author}</Text>
                          </Space>
                        </List.Item>
                      )}
                      style={{ 
                        backgroundColor: '#fafafa',
                        borderRadius: '4px'
                      }}
                    />
                  </div>
                )}
              </Space>
            </Card>
          )}
        </Space>
      </Card>
    </div>
  );
};

export default AuthorSearch;

