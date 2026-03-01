import { Input, Typography } from 'antd';
import { SearchOutlined } from '@ant-design/icons';

const { Title } = Typography;
// 搜索框组件
// 接收value和onChange两个属性，用于控制输入框的值和输入框值变化时的回调函数
const BookSearch = ({ value, onChange }) => {
  return (
    <>
      <Title level={2} style={{ textAlign: 'center', marginBottom: 24 }}>
        热门书籍推荐
      </Title>
      <div style={{ marginBottom: 24 }}>
        <Input.Search
          placeholder="搜索书籍名称" // 输入框的提示文本
          allowClear // 是否显示清空按钮
          enterButton={<SearchOutlined />}
          size="large"
          value={value}
          onChange={onChange}
          style={{ maxWidth: 1500, margin: '0 auto' }}
        />
      </div>
    </>
  )
}

export default BookSearch