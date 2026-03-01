import React, { useState } from 'react'
import { Card, Form, Input, DatePicker, Button, Space, Row, Col } from 'antd'
import { SearchOutlined, ClearOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'

const { RangePicker } = DatePicker

const OrderSearch = ({ onSearch, loading }) => {
  const [form] = Form.useForm()
  const [searchParams, setSearchParams] = useState({})

  // 处理搜索
  const handleSearch = () => {
    const values = form.getFieldsValue()
    
    const params = {
      bookTitle: values.bookTitle?.trim() || '',
      startTime: values.dateRange?.[0]?.format('YYYY-MM-DD') || '',
      endTime: values.dateRange?.[1]?.format('YYYY-MM-DD') || ''
    }
    
    setSearchParams(params)
    onSearch(params)
  }

  // 重置搜索
  const handleReset = () => {
    form.resetFields()
    setSearchParams({})
    onSearch({
      bookTitle: '',
      startTime: '',
      endTime: ''
    })
  }

  // 快捷时间选择
  const quickRanges = {
    '今天': [dayjs().startOf('day'), dayjs().endOf('day')],
    '最近7天': [dayjs().subtract(7, 'days').startOf('day'), dayjs().endOf('day')],
    '最近30天': [dayjs().subtract(30, 'days').startOf('day'), dayjs().endOf('day')],
    '本月': [dayjs().startOf('month'), dayjs().endOf('month')],
    '上月': [dayjs().subtract(1, 'month').startOf('month'), dayjs().subtract(1, 'month').endOf('month')]
  }

  return (
    <Card 
      title="订单搜索" 
      style={{ marginBottom: 16 }}
      bodyStyle={{ paddingBottom: 16 }}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSearch}
      >
        <Row gutter={16}>
          <Col xs={24} sm={12} md={8}>
            <Form.Item
              label="书籍名称"
              name="bookTitle"
            >
              <Input 
                placeholder="请输入书籍名称"
                allowClear
              />
            </Form.Item>
          </Col>
          <Col xs={24} sm={12} md={10}>
            <Form.Item
              label="时间范围"
              name="dateRange"
            >
              <RangePicker
                style={{ width: '100%' }}
                placeholder={['开始日期', '结束日期']}
                presets={quickRanges}
                format="YYYY-MM-DD"
              />
            </Form.Item>
          </Col>
          <Col xs={24} sm={24} md={6}>
            <Form.Item label=" " style={{ marginBottom: 0 }}>
              <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
                <Button 
                  type="primary" 
                  icon={<SearchOutlined />}
                  onClick={handleSearch}
                  loading={loading}
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
            </Form.Item>
          </Col>
        </Row>
      </Form>
      
      {/* 显示当前搜索条件 */}
      {(searchParams.bookTitle || searchParams.startTime || searchParams.endTime) && (
        <div style={{ 
          marginTop: 12, 
          padding: 8, 
          background: '#f5f5f5', 
          borderRadius: 4,
          fontSize: '12px',
          color: '#666'
        }}>
          <strong>当前搜索条件：</strong>
          {searchParams.bookTitle && <span>书籍名称："{searchParams.bookTitle}" </span>}
          {searchParams.startTime && searchParams.endTime && (
            <span>时间范围：{searchParams.startTime} 至 {searchParams.endTime}</span>
          )}
        </div>
      )}
    </Card>
  )
}

export default OrderSearch
