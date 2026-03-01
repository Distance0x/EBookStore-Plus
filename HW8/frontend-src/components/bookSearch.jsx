import { Input, Typography, Select, Space, Tag, Divider } from 'antd';
import { SearchOutlined, TagsOutlined } from '@ant-design/icons';
import { useState, useEffect } from 'react';
import BookService from '../service/bookService.jsx';

const { Title } = Typography;

// 搜索框组件
// 接收value和onChange两个属性，用于控制输入框的值和输入框值变化时的回调函数
// 新增 onTagSearch 属性，用于处理标签搜索
// 新增 selectedTag 和 onTagChange 属性，用于控制选中的标签
const BookSearch = ({ 
  value, 
  onChange, 
  onTagSearch, 
  selectedTag, 
  onTagChange,
  searchMode,
  onSearchModeChange
}) => {
  const [tags, setTags] = useState([]);
  const [loadingTags, setLoadingTags] = useState(false);

  // 加载所有标签
  useEffect(() => {
    const fetchTags = async () => {
      setLoadingTags(true);
      try {
        const data = await BookService.getAllTags();
        setTags(data || []);
      } catch (error) {
        console.error('Failed to fetch tags:', error);
      } finally {
        setLoadingTags(false);
      }
    };
    fetchTags();
  }, []);

  // 处理标签选择
  const handleTagSelect = (tag) => {
    onTagChange(tag);
    if (tag && onTagSearch) {
      onTagSearch(tag);
    }
  };

  // 清除标签选择
  const handleTagClear = () => {
    onTagChange(null);
  };

  return (
    <>
      <Title level={2} style={{ textAlign: 'center', marginBottom: 24 }}>
        热门书籍推荐
      </Title>
      
      {/* 搜索模式切换 */}
      <div style={{ marginBottom: 16, textAlign: 'center' }}>
        <Space>
          <Tag 
            color={searchMode === 'keyword' ? 'blue' : 'default'} 
            style={{ cursor: 'pointer', padding: '4px 12px', fontSize: '14px' }}
            onClick={() => onSearchModeChange('keyword')}
          >
            <SearchOutlined /> 关键字搜索
          </Tag>
          <Tag 
            color={searchMode === 'tag' ? 'blue' : 'default'} 
            style={{ cursor: 'pointer', padding: '4px 12px', fontSize: '14px' }}
            onClick={() => onSearchModeChange('tag')}
          >
            <TagsOutlined /> 标签搜索
          </Tag>
        </Space>
      </div>

      <div style={{ marginBottom: 24 }}>
        {searchMode === 'keyword' ? (
          // 关键字搜索模式
          <Input.Search
            placeholder="搜索书籍名称"
            allowClear
            enterButton={<SearchOutlined />}
            size="large"
            value={value}
            onChange={onChange}
            style={{ maxWidth: 1500, margin: '0 auto' }}
          />
        ) : (
          // 标签搜索模式
          <div style={{ maxWidth: 1500, margin: '0 auto' }}>
            <Select
              showSearch
              allowClear
              placeholder="选择标签（将自动关联相关标签进行搜索）"
              size="large"
              style={{ width: '100%' }}
              loading={loadingTags}
              value={selectedTag}
              onChange={handleTagSelect}
              onClear={handleTagClear}
              filterOption={(input, option) => {
                // 确保 option.children 是字符串类型
                const label = option?.children;
                if (typeof label === 'string') {
                  return label.toLowerCase().includes(input.toLowerCase());
                }
                // 如果是数组（包含图标和文本），提取文本部分
                if (Array.isArray(label)) {
                  const text = label.find(item => typeof item === 'string');
                  return text ? text.toLowerCase().includes(input.toLowerCase()) : false;
                }
                return false;
              }}
            >
              {tags.map(tag => (
                <Select.Option key={tag} value={tag}>
                  <TagsOutlined style={{ marginRight: 8 }} />
                  {tag}
                </Select.Option>
              ))}
            </Select>
            
            {selectedTag && (
              <div style={{ marginTop: 12, textAlign: 'center' }}>
                <Space>
                  <span style={{ color: '#666' }}>当前选中标签：</span>
                  <Tag color="blue" closable onClose={handleTagClear}>
                    {selectedTag}
                  </Tag>
                  <span style={{ color: '#999', fontSize: '12px' }}>
                    （系统将自动关联相关标签进行搜索）
                  </span>
                </Space>
              </div>
            )}
          </div>
        )}
      </div>
    </>
  )
}

export default BookSearch