import { Typography, Button, Divider } from 'antd'

const { Text } = Typography

// 购物车结算组件
// 接收total和onCheckout两个属性，用于控制总价和结算按钮的回调函数
const CartSummary = ({ total, onCheckout, disabled }) => {
  return (
    <>
      <Divider /> 
      {/* // 分割线组件喵 */}
      <div style={{ textAlign: 'right', marginTop: 16 }}>
        <Text strong style={{ fontSize: 18 }}>
          总计: ¥{total}
        </Text>
        <Button 
          type="primary" 
          size="large" 
          style={{ marginLeft: 16 }}
          disabled={disabled}
          onClick={onCheckout}
        >
          去结算
        </Button>
      </div>
    </>
  )
}

export default CartSummary