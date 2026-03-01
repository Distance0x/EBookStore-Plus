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
  Avatar,
  Switch,
  Tag,
  Pagination
} from "antd";
import { 
  BarChartOutlined, 
  BookOutlined,
  UserOutlined, 
  DollarOutlined, 
  ShoppingOutlined, 
  TrophyOutlined,
  ClearOutlined,
  TableOutlined,
  TeamOutlined
} from "@ant-design/icons";
import { Column } from '@ant-design/charts';
import AdminService from '../service/AdminService.jsx';
import useAdminSessionCheck from '../hooks/useAdminSessionCheck.jsx';

const { RangePicker } = DatePicker;
const { Title } = Typography;

const UserConsumptionStatistics = () => {
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
      const response = await AdminService.getUserConsumptionStatisticsWithGlobal(
        startTime,
        endTime,
        page - 1, // 后端页码从0开始
        pageSize
      );
      
      // 新的响应格式包含page和globalStatistics
      const pageData = response.page;
      const globalStats = response.globalStatistics;
      
      // 处理分页数据 - 每个content项是一个UserConsumptionStatisticsDTO，包含单个用户的消费统计
      if (pageData.content && pageData.content.length > 0) {
        // 提取所有用户消费数据
        const allUserConsumptions = pageData.content.map(dto => dto.userConsumptions[0]).filter(Boolean);
        
        // 使用全局统计数据
        const totalStats = {
          totalUsers: globalStats.totalBookTypes, // 这里表示消费用户总数
          totalOrders: globalStats.totalOrderCount,
          totalRevenue: globalStats.totalRevenue,
          totalBooks: globalStats.totalBooksSold,
          userConsumptions: allUserConsumptions
        };
        
        setStatistics(totalStats);
      } else {
        setStatistics({
          totalUsers: 0,
          totalOrders: 0,
          totalRevenue: 0,
          totalBooks: 0,
          userConsumptions: []
        });
      }
      
      setTotalRecords(pageData.totalElements);
      setCurrentPage(page);
      message.success('统计数据获取成功');
    } catch (error) {
      message.error("获取用户消费统计失败: " + error.message);
      setStatistics(null);
    } finally {
      setLoading(false);
    }
  };

  // 获取统计数据（非分页，保留用于兼容）
  const fetchStatisticsOld = async (startTime = null, endTime = null) => {
    setLoading(true);
    try {
      const response = await AdminService.getUserConsumptionStatistics(startTime, endTime);
      setStatistics(response);
      message.success('统计数据获取成功');
    } catch (error) {
      message.error("获取用户消费统计失败: " + error.message);
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

  // 准备柱状图数据（消费排行榜前15）
  const getColumnChartData = () => {
    if (!statistics?.userConsumptions) return [];
    
    return statistics.userConsumptions
      .slice(0, 15) // 显示前15名
      .map((user, index) => ({
        user: user.name || user.username || `用户${user.userId}`,
        consumption: Number(user.totalSpent),
        rank: index + 1,
        orderCount: user.orderCount,
        username: user.username
      }));
  };

  // 柱状图配置（消费金额）
  const columnConfig = {
    data: getColumnChartData(),
    xField: 'user',
    yField: 'consumption',
    label: {
      position: 'middle',
      style: {
        fill: '#FFFFFF',
        opacity: 0.8,
        fontWeight: 'bold'
      },
      formatter: (datum) => `¥${datum.consumption.toFixed(0)}`
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
        formatter: (v) => `¥${Number(v).toLocaleString()}`,
      },
    },
    meta: {
      user: {
        alias: '用户',
      },
      consumption: {
        alias: '消费金额',
      },
    },
    color: '#52c41a',
    columnStyle: {
      radius: [4, 4, 0, 0],
    },
    tooltip: {
      formatter: (datum) => {
        return {
          name: '消费金额',
          value: `¥${Number(datum.consumption).toLocaleString()}`
        };
      },
      title: (title, datum) => `${datum?.username || title} (${datum?.orderCount}单)`
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
      title: '用户信息',
      key: 'userInfo',
      width: 200,
      render: (_, record) => (
        <Space>
          <Avatar 
            size={40} 
            icon={<UserOutlined />} 
            style={{ backgroundColor: '#1890ff' }}
          />
          <div>
            <div style={{ fontWeight: '500', fontSize: '14px' }}>
              {record.name || record.username || `用户${record.userId}`}
            </div>
            <div style={{ color: '#666', fontSize: '12px' }}>
              @{record.username}
            </div>
          </div>
        </Space>
      ),
    },
    {
      title: '联系方式',
      key: 'contact',
      width: 160,
      render: (_, record) => (
        <div>
          <div style={{ fontSize: '12px', color: '#666' }}>
            📧 {record.email || '未设置'}
          </div>
          <div style={{ fontSize: '12px', color: '#666', marginTop: 2 }}>
            📱 {record.phone || '未设置'}
          </div>
        </div>
      ),
    },    {
      title: '累计消费',
      dataIndex: 'totalSpent',
      key: 'totalSpent',
      width: 120,
      align: 'right',
      render: (amount, record, index) => {
        // 计算全局排名：(当前页码-1) * 每页大小 + 当前索引 + 1
        const globalRank = (currentPage - 1) * pageSize + index + 1;
        return (
          <span style={{ 
            fontWeight: 'bold', 
            color: globalRank <= 3 ? '#f5222d' : '#52c41a', 
            fontSize: globalRank <= 3 ? '16px' : '14px'
          }}>
            ¥{Number(amount).toLocaleString()}
          </span>
        );
      },
      sorter: (a, b) => Number(a.totalSpent) - Number(b.totalSpent),
    },
    {
      title: '订单数量',
      dataIndex: 'orderCount',
      key: 'orderCount',
      width: 100,
      align: 'center',
      render: (count) => (
        <Tag color="blue">{count}单</Tag>
      ),
      sorter: (a, b) => a.orderCount - b.orderCount,
    },
    {
      title: '购书总数',
      dataIndex: 'totalBooksCount',
      key: 'totalBooksCount',
      width: 100,
      align: 'center',
      render: (count) => (
        <Tag color="green">{count}本</Tag>
      ),
      sorter: (a, b) => a.totalBooksCount - b.totalBooksCount,
    },
    {
      title: '平均订单额',
      dataIndex: 'averageOrderValue',
      key: 'averageOrderValue',
      width: 120,
      align: 'right',
      render: (avg) => (
        <span style={{ color: '#722ed1', fontWeight: '500' }}>
          ¥{Number(avg).toFixed(2)}
        </span>
      ),
      sorter: (a, b) => Number(a.averageOrderValue) - Number(b.averageOrderValue),
    },
  ];

  return (
    <div style={{ padding: '24px', backgroundColor: '#f5f5f5', minHeight: '100vh' }}>
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
            <TeamOutlined style={{ fontSize: '24px', color: '#1890ff', marginRight: 12 }} />
            <Title level={3} style={{ margin: 0 }}>
              用户消费统计 - 消费排行榜
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
        <>          {/* 总体统计数据 */}
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={12} lg={6}>
              <Card size="small">
                <Statistic
                  title="活跃用户数"
                  value={statistics.totalUsers}
                  prefix={<TeamOutlined style={{ color: '#1890ff' }} />}
                  valueStyle={{ color: '#1890ff', fontSize: '24px', fontWeight: 'bold' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card size="small">
                <Statistic
                  title="总订单数"
                  value={statistics.totalOrders}
                  prefix={<ShoppingOutlined style={{ color: '#52c41a' }} />}
                  valueStyle={{ color: '#52c41a', fontSize: '24px', fontWeight: 'bold' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card size="small">
                <Statistic
                  title="总销售册数"
                  value={statistics.totalBooks}
                  prefix={<BookOutlined style={{ color: '#52c41a' }} />}
                  valueStyle={{ color: '#52c41a', fontSize: '24px', fontWeight: 'bold' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card size="small">
                <Statistic
                  title="总收入"
                  value={statistics.totalRevenue}
                  prefix={<DollarOutlined style={{ color: '#f5222d' }} />}
                  suffix="元"
                  precision={2}
                  valueStyle={{ color: '#f5222d', fontSize: '24px', fontWeight: 'bold' }}
                />
              </Card>
            </Col>
          </Row>

          {/* 图表/表格切换 */}
          <Card>
            <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Title level={4} style={{ margin: 0 }}>
                <TrophyOutlined style={{ marginRight: 8, color: '#faad14' }} />
                消费排行榜详情
              </Title>
              <Space>
                <span>显示模式：</span>
                <Switch
                  checkedChildren={<BarChartOutlined />}
                  unCheckedChildren={<TableOutlined />}
                  checked={viewMode === 'chart'}
                  onChange={(checked) => setViewMode(checked ? 'chart' : 'table')}
                />
                <span>{viewMode === 'chart' ? '图表' : '表格'}</span>
              </Space>
            </div>

            {viewMode === 'chart' ? (
              // 图表模式
              <Card title="消费排行榜 - 累计消费对比（前15名）" size="small">
                {getColumnChartData().length > 0 ? (
                  <Column {...columnConfig} height={400} />
                ) : (
                  <Empty description="暂无消费数据" />
                )}
              </Card>            ) : (
              // 表格模式
              <div>
                {statistics.userConsumptions && statistics.userConsumptions.length > 0 ? (
                  <>                    <Table
                      columns={columns}
                      dataSource={statistics.userConsumptions}
                      rowKey="userId"
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
                            `第 ${range[0]}-${range[1]} 条，共 ${total} 位用户`
                          }
                        />
                      </div>
                    )}
                  </>
                ) : (
                  <Empty 
                    description="暂无消费记录" 
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
                  请选择时间范围并点击"开始统计"按钮查看用户消费统计数据
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

export default UserConsumptionStatistics;