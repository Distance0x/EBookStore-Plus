import React, { useState, useEffect } from "react";
import { Card, DatePicker, Row, Col, Statistic, Table, message, Button, Empty, Pagination } from "antd";
import { ShoppingCartOutlined, BookOutlined, DollarOutlined, BarChartOutlined } from "@ant-design/icons";
import OrderService from "../service/orderService";
import dayjs from "dayjs";

const { RangePicker } = DatePicker;

const PurchaseStatistics = () => {
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState([]);
  
  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);  // 获取统计数据（分页）
  const fetchStatistics = async (startTime = null, endTime = null, page = 1) => {
    setLoading(true);
    try {
      const response = await OrderService.getUserPurchaseStatisticsWithGlobal(
        startTime,
        endTime,
        page - 1, // 后端页码从0开始
        pageSize
      );      
      
      // 新的响应格式包含page和globalStatistics
      const pageData = response.page;
      const globalStats = response.globalStatistics;
      
      // 处理分页数据 - 现在每个订单对应一本书的聚合统计
      if (pageData.content && pageData.content.length > 0) {
        // 提取所有书籍统计数据（现在每个DTO只包含一本书的聚合数据）
        const allBookStatistics = pageData.content.map(dto => dto.bookStatistics[0]).filter(Boolean);
        
        // 使用全局统计数据
        setStatistics({
          totalBooks: globalStats.totalBooksSold,  // 总购买图书数
          totalAmount: globalStats.totalRevenue,   // 总消费金额
          totalBookTypes: globalStats.totalBookTypes, // 购买图书种类
          bookStatistics: allBookStatistics
        });
      } else {
        setStatistics({
          totalBooks: 0,
          totalAmount: 0,
          totalBookTypes: 0,
          bookStatistics: []
        });
      }
      
      setTotalRecords(pageData.totalElements);
      setCurrentPage(page);
    } catch (error) {
      message.error("获取购买统计失败: " + error.message);
    } finally {
      setLoading(false);
    }
  };// 处理统计按钮点击
  const handleStatistics = () => {
    if (dateRange && dateRange.length === 2) {
      const startTime = dateRange[0].format('YYYY-MM-DD');
      const endTime = dateRange[1].format('YYYY-MM-DD');
      fetchStatistics(startTime, endTime, 1);
    } else {
      fetchStatistics(null, null, 1);
    }
  };

  // 处理分页变化
  const handlePageChange = (page) => {
    if (dateRange && dateRange.length === 2) {
      const startTime = dateRange[0].format('YYYY-MM-DD');
      const endTime = dateRange[1].format('YYYY-MM-DD');
      fetchStatistics(startTime, endTime, page);
    } else {
      fetchStatistics(null, null, page);
    }
  };

  // 处理日期范围选择
  const handleDateRangeChange = (dates) => {
    setDateRange(dates);
  };
  
  // 清除日期筛选
  const handleClearFilter = () => {
    setDateRange([]);
    setStatistics(null); // 清除当前统计数据
    setCurrentPage(1);
    setTotalRecords(0);
  };
  // 表格列定义
  const columns = [
    {
      title: '封面',
      dataIndex: 'bookCover',
      key: 'bookCover',
      width: 80,
      render: (cover) => (
        <img 
          src={cover} 
          alt="book cover" 
          style={{ width: 50, height: 70, objectFit: 'cover' }}
        />
      ),
    },
    {
      title: '书名',
      dataIndex: 'bookTitle',
      key: 'bookTitle',
      width: 200,
    },
    {
      title: '作者',
      dataIndex: 'bookAuthor',
      key: 'bookAuthor',
      width: 120,
    },
    {
      title: '购买数量',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
      render: (quantity) => (
        <span style={{ fontWeight: 'bold', color: '#1890ff' }}>
          {quantity}
        </span>
      ),
    },
    {
      title: '总价',
      dataIndex: 'totalPrice',
      key: 'totalPrice',
      width: 120,
      render: (price) => (
        <span style={{ fontWeight: 'bold', color: '#52c41a' }}>
          ¥{price}
        </span>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Card 
        title={
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <BarChartOutlined style={{ marginRight: 8 }} />
            购买统计
          </div>
        }        extra={
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <RangePicker
              value={dateRange}
              onChange={handleDateRangeChange}
              placeholder={['开始日期', '结束日期']}
              style={{ width: 250 }}
            />
            <Button 
              type="primary" 
              icon={<BarChartOutlined />}
              onClick={handleStatistics}
              loading={loading}
            >
              开始统计
            </Button>
            {dateRange.length > 0 && (
              <Button onClick={handleClearFilter}>
                清除筛选
              </Button>
            )}
          </div>
        }
        style={{ marginBottom: 24 }}
      >        {statistics ? (
          <>
            <Row gutter={16} style={{ marginBottom: 24 }}>
              <Col xs={24} sm={8}>
                <Card>
                  <Statistic
                    title="总购买图书数"
                    value={statistics.totalBooks}
                    prefix={<BookOutlined />}
                    valueStyle={{ color: '#1890ff' }}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={8}>
                <Card>
                  <Statistic
                    title="总消费金额"
                    value={statistics.totalAmount}
                    prefix={<DollarOutlined />}
                    suffix="元"
                    precision={2}
                    valueStyle={{ color: '#52c41a' }}
                  />
                </Card>
              </Col>              <Col xs={24} sm={8}>
                <Card>
                  <Statistic
                    title="购买图书种类"
                    value={statistics.totalBookTypes || 0}
                    prefix={<ShoppingCartOutlined />}
                    valueStyle={{ color: '#722ed1' }}
                  />
                </Card>
              </Col>
            </Row>            {/* 详细统计表格 */}
            <Card title="详细购买记录" size="small">
              {statistics.bookStatistics && statistics.bookStatistics.length > 0 ? (
                <>
                  <Table
                    columns={columns}
                    dataSource={statistics.bookStatistics}
                    rowKey={(record) => `${record.bookTitle}-${record.bookAuthor}`}
                    loading={loading}
                    pagination={false} // 关闭表格内置分页
                    scroll={{ x: 600 }}
                  />
                  
                  {/* 自定义分页组件 */}
                  {totalRecords > 0 && (
                    <div style={{ textAlign: 'center', marginTop: 16 }}>
                      <Pagination
                        current={currentPage}
                        total={totalRecords}
                        pageSize={pageSize}
                        onChange={handlePageChange}
                        showSizeChanger={false}
                        showQuickJumper
                        showTotal={(total, range) => 
                          `第 ${range[0]}-${range[1]} 条，共 ${total} 条记录`
                        }
                      />
                    </div>
                  )}
                </>
              ) : (
                <Empty description="暂无购买记录" />
              )}
            </Card>
          </>
        ) : (
          <div style={{ textAlign: 'center', padding: '50px 0' }}>
            <Empty 
              description={
                <div>
                  <p>请选择时间范围并点击"开始统计"按钮查看购买统计数据</p>
                  <p style={{ color: '#666', fontSize: '14px' }}>不选择时间范围将统计全部数据</p>
                </div>
              }
            />
          </div>
        )}
      </Card>
    </div>
  );
};

export default PurchaseStatistics;
