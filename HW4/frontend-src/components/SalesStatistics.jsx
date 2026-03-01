import React, { useState } from "react";
import { 
  Card, 
  DatePicker, 
  Row, 
  Col, 
  Statistic, 
  Table, 
  message, 
  Button, 
  Empty,
  Space,
  Typography,
  Image,
  Switch,
  Pagination
} from "antd";
import { 
  BarChartOutlined, 
  BookOutlined, 
  DollarOutlined, 
  ShoppingOutlined, 
  TrophyOutlined,
  ClearOutlined,
  TableOutlined
} from "@ant-design/icons";
import { Column } from '@ant-design/charts';
import AdminService from '../service/AdminService.jsx';
import useAdminSessionCheck from '../hooks/useAdminSessionCheck.jsx';

const { RangePicker } = DatePicker;
const { Title } = Typography;

const SalesStatistics = () => {
  // 使用管理员session检查hook
  useAdminSessionCheck();
  
  const [statistics, setStatistics] = useState(null);
  const [loading, setLoading] = useState(false);
  const [dateRange, setDateRange] = useState([]);
  const [viewMode, setViewMode] = useState('table'); // 'chart' 或 'table'
  
  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);
  const [totalRecords, setTotalRecords] = useState(0);  // 获取统计数据（分页）
  const fetchStatistics = async (startTime = null, endTime = null, page = 1) => {
    setLoading(true);
    try {
      const response = await AdminService.getBookSalesStatisticsWithGlobal(
        startTime,
        endTime,
        page - 1, // 后端页码从0开始
        pageSize
      );
      
      // 新的响应格式包含page和globalStatistics
      const pageData = response.page;
      const globalStats = response.globalStatistics;
      
      // 处理分页数据
      if (pageData.content && pageData.content.length > 0) {
        // 提取所有书籍销量数据
        const allBookSales = pageData.content
          .map(dto => dto.bookSales && dto.bookSales.length > 0 ? dto.bookSales[0] : null)
          .filter(Boolean);
          
        // 使用全局统计数据
        const totalStats = {
          totalBooks: globalStats.totalBookTypes, // 在售书籍种类数
          totalSales: globalStats.totalBooksSold, // 总销售册数
          totalRevenue: globalStats.totalRevenue, // 总销售收入
          totalOrders: globalStats.totalOrderCount, // 总订单数
          // 为了兼容显示组件，也设置这些字段
          totalOrderCount: globalStats.totalOrderCount,
          totalBooksSold: globalStats.totalBooksSold,
          bookSales: allBookSales
        };
        
        setStatistics(totalStats);
      } else {
        setStatistics({
          totalBooks: 0,
          totalSales: 0,
          totalRevenue: 0,
          totalOrders: 0,
          totalOrderCount: 0,
          totalBooksSold: 0,
          bookSales: []
        });
      }
      
      setTotalRecords(pageData.totalElements);
      setCurrentPage(page);
      message.success('统计数据获取成功');
    } catch (error) {
      message.error("获取销量统计失败: " + error.message);
      setStatistics(null);
    } finally {
      setLoading(false);
    }
  };

  // 获取统计数据（非分页，保留用于兼容）
  const fetchStatisticsOld = async (startTime = null, endTime = null) => {
    setLoading(true);
    try {
      const response = await AdminService.getBookSalesStatistics(startTime, endTime);
      setStatistics(response);
      message.success('统计数据获取成功');
    } catch (error) {
      message.error("获取销量统计失败: " + error.message);
      setStatistics(null);
    } finally {
      setLoading(false);
    }
  };
  // 处理统计按钮点击
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
    setStatistics(null);
    setCurrentPage(1);
    setTotalRecords(0);
  };
  // 准备柱状图数据（销量排行榜前10）
  const getColumnChartData = () => {
    if (!statistics?.bookSales) return [];
    
    return statistics.bookSales
      .slice(0, 10) // 只显示前10名
      .map((book, index) => ({
        book: book.bookTitle.length > 8 ? book.bookTitle.substring(0, 8) + '...' : book.bookTitle,
        sales: book.quantitySold,
        revenue: Number(book.revenue),
        rank: index + 1,
        fullTitle: book.bookTitle,
        author: book.bookAuthor
      }));
  };
  // 柱状图配置（销量）
  const columnConfig = {
    data: getColumnChartData(),
    xField: 'book',
    yField: 'sales',
    label: {
      position: 'middle',
      style: {
        fill: '#FFFFFF',
        opacity: 0.8,
        fontWeight: 'bold'
      },
    },
    xAxis: {
      label: {
        autoRotate: true,
        autoHide: true,
        style: {
          fontSize: 12,
        }
      },
    },
    yAxis: {
      label: {
        formatter: (v) => `${v}本`,
      },
    },
    meta: {
      book: {
        alias: '书籍',
      },
      sales: {
        alias: '销量',
      },
    },
    color: '#1890ff',
    columnStyle: {
      radius: [4, 4, 0, 0],
    },
    tooltip: {
      formatter: (datum) => {
        return {
          name: '销量',
          value: `${datum.sales}本`
        };
      },
      title: (title, datum) => datum?.fullTitle || title
    }
  };

  // 表格列定义
  const columns = [    {
      title: '排名',
      key: 'rank',
      width: 80,
      align: 'center',
      render: (_, record, index) => {
        // 计算全局排名：(当前页码-1) * 每页大小 + 当前索引 + 1
        const globalRank = (currentPage - 1) * pageSize + index + 1;
        let color = '#666';
        let icon = null;
        
        if (globalRank === 1) {
          color = '#faad14';
          icon = <TrophyOutlined style={{ color, marginRight: 4 }} />;
        } else if (globalRank === 2) {
          color = '#c0c0c0';
          icon = <TrophyOutlined style={{ color, marginRight: 4 }} />;
        } else if (globalRank === 3) {
          color = '#cd7f32';
          icon = <TrophyOutlined style={{ color, marginRight: 4 }} />;
        }
        
        return (
          <Space>
            {icon}
            <span style={{ 
              fontWeight: globalRank <= 3 ? 'bold' : 'normal',
              color: globalRank <= 3 ? color : '#666',
              fontSize: globalRank <= 3 ? '16px' : '14px'
            }}>
              {globalRank}
            </span>
          </Space>
        );
      },
    },
    {
      title: '封面',
      dataIndex: 'bookCover',
      key: 'bookCover',
      width: 100,
      align: 'center',
      render: (cover, record) => (
        <Image 
          src={cover || '/placeholder-book.png'} 
          alt={record.bookTitle}
          width={60}
          height={80}
          style={{ objectFit: 'cover' }}
          fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMIAAADDCAYAAADQvc6U..."
        />
      ),
    },
    {
      title: '书名',
      dataIndex: 'bookTitle',
      key: 'bookTitle',
      width: 200,
      ellipsis: true,
      render: (title) => (
        <span style={{ fontWeight: '500' }}>{title}</span>
      ),
    },
    {
      title: '作者',
      dataIndex: 'bookAuthor',
      key: 'bookAuthor',
      width: 120,
      ellipsis: true,
    },
    {
      title: '出版社',
      dataIndex: 'publisher',
      key: 'publisher',
      width: 120,
      ellipsis: true,
    },
    {
      title: '当前价格',
      dataIndex: 'price',
      key: 'price',
      width: 100,
      align: 'right',
      render: (price) => (
        <span style={{ color: '#1890ff', fontWeight: '500' }}>
          ¥{Number(price).toFixed(2)}
        </span>
      ),
    },    {
      title: '销售数量',
      dataIndex: 'quantitySold',
      key: 'quantitySold',
      width: 100,
      align: 'center',
      render: (quantity, record, index) => {
        // 计算全局排名：(当前页码-1) * 每页大小 + 当前索引 + 1
        const globalRank = (currentPage - 1) * pageSize + index + 1;
        return (
          <span style={{ 
            fontWeight: 'bold', 
            color: globalRank <= 3 ? '#f5222d' : '#52c41a', 
            fontSize: globalRank <= 3 ? '18px' : '16px'
          }}>
            {quantity}
          </span>
        );
      },
      sorter: (a, b) => a.quantitySold - b.quantitySold,
    },
    {
      title: '销售收入',
      dataIndex: 'revenue',
      key: 'revenue',
      width: 120,
      align: 'right',
      render: (revenue, record, index) => {
        // 计算全局排名：(当前页码-1) * 每页大小 + 当前索引 + 1
        const globalRank = (currentPage - 1) * pageSize + index + 1;
        return (
          <span style={{ 
            fontWeight: globalRank <= 3 ? 'bold' : '500', 
            color: globalRank <= 3 ? '#f5222d' : '#f5222d'
          }}>
            ¥{Number(revenue).toFixed(2)}
          </span>
        );
      },
      sorter: (a, b) => Number(a.revenue) - Number(b.revenue),
    },
    {
      title: '订单数',
      dataIndex: 'orderCount',
      key: 'orderCount',
      width: 80,
      align: 'center',
      render: (count) => (
        <span style={{ color: '#722ed1', fontWeight: '500' }}>{count}</span>
      ),
    },
  ];

  return (
    <div>
      {/* 标题和操作区域 */}
      <Card 
        style={{ marginBottom: 24 }}
        bodyStyle={{ padding: '20px 24px' }}
      >
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '16px'
        }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <BarChartOutlined style={{ fontSize: '24px', color: '#1890ff', marginRight: 12 }} />
            <Title level={3} style={{ margin: 0 }}>
              书籍销量统计 - 热销榜
            </Title>
          </div>
          
          <Space size="middle" wrap>
            <RangePicker
              value={dateRange}
              onChange={handleDateRangeChange}
              placeholder={['开始日期', '结束日期']}
              style={{ width: 280 }}
            />
            <Button 
              type="primary" 
              icon={<BarChartOutlined />}
              onClick={handleStatistics}
              loading={loading}
              size="large"
            >
              开始统计
            </Button>
            {(dateRange.length > 0 || statistics) && (
              <Button 
                icon={<ClearOutlined />}
                onClick={handleClearFilter}
                size="large"
              >
                清除筛选
              </Button>
            )}
          </Space>
        </div>
      </Card>

      {statistics ? (
        <>
          {/* 总体统计数据 */}
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={12} lg={6}>
              <Card size="small">
                <Statistic
                  title="总订单数"
                  value={statistics.totalOrderCount}
                  prefix={<ShoppingOutlined style={{ color: '#1890ff' }} />}
                  valueStyle={{ color: '#1890ff', fontSize: '24px', fontWeight: 'bold' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card size="small">
                <Statistic
                  title="总销售册数"
                  value={statistics.totalBooksSold}
                  prefix={<BookOutlined style={{ color: '#52c41a' }} />}
                  valueStyle={{ color: '#52c41a', fontSize: '24px', fontWeight: 'bold' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card size="small">
                <Statistic
                  title="总销售收入"
                  value={statistics.totalRevenue}
                  prefix={<DollarOutlined style={{ color: '#f5222d' }} />}
                  suffix="元"
                  precision={2}
                  valueStyle={{ color: '#f5222d', fontSize: '24px', fontWeight: 'bold' }}
                />
              </Card>
            </Col>            <Col xs={24} sm={12} lg={6}>
              <Card size="small">
                <Statistic
                  title="参与销售书籍种类"
                  value={statistics.totalBooks}
                  prefix={<TrophyOutlined style={{ color: '#722ed1' }} />}
                  valueStyle={{ color: '#722ed1', fontSize: '24px', fontWeight: 'bold' }}
                />
              </Card>
            </Col>
          </Row>

          {/* 图表/表格切换 */}
          <Card>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Title level={4} style={{ margin: 0 }}>
                <TrophyOutlined style={{ marginRight: 8, color: '#faad14' }} />
                热销统计详情
              </Title>              <Space>
                <span>显示模式：</span>
                <Switch
                  checkedChildren={<BarChartOutlined />}
                  unCheckedChildren={<TableOutlined />}
                  checked={viewMode === 'chart'}
                  onChange={(checked) => setViewMode(checked ? 'chart' : 'table')}
                />
                <span>{viewMode === 'chart' ? '图表' : '表格'}</span>
              </Space>
            </div>            {viewMode === 'chart' ? (
              // 图表模式 - 只保留销量排行榜
              <Card title="热销排行榜 - 销量对比（前10名）" size="small">
                {getColumnChartData().length > 0 ? (
                  <Column {...columnConfig} height={400} />
                ) : (
                  <Empty description="暂无销售数据" />
                )}
              </Card>
            ) : (              // 表格模式
              <div>
                {statistics.bookSales && statistics.bookSales.length > 0 ? (
                  <>                    <Table
                      columns={columns}
                      dataSource={statistics.bookSales}
                      rowKey="bookId"
                      loading={loading}
                      pagination={false} // 禁用内置分页
                      scroll={{ x: 1000 }}
                      size="middle"
                      rowClassName={(record, index) => {
                        // 计算全局排名：(当前页码-1) * 每页大小 + 当前索引 + 1
                        const globalRank = (currentPage - 1) * pageSize + index + 1;
                        return globalRank <= 3 ? 'ant-table-row-selected' : '';
                      }}
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
                            `第 ${range[0]}-${range[1]} 条，共 ${total} 种书籍`
                          }
                        />
                      </div>
                    )}
                  </>
                ) : (
                  <Empty 
                    description="暂无销售记录" 
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                    style={{ padding: '40px 0' }}
                  />
                )}
              </div>
            )}
          </Card>
        </>
      ) : (
        <Card style={{ textAlign: 'center', padding: '60px 20px' }}>
          <Empty 
            description={
              <div>
                <p style={{ fontSize: '16px', margin: '16px 0 8px' }}>
                  请选择时间范围并点击"开始统计"按钮查看销量统计数据
                </p>
                <p style={{ color: '#999', fontSize: '14px', margin: 0 }}>
                  不选择时间范围将统计全部数据
                </p>
              </div>
            }
            image={Empty.PRESENTED_IMAGE_DEFAULT}
          />
        </Card>
      )}
    </div>
  );
};

export default SalesStatistics;
