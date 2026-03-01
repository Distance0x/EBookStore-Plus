import React from 'react';
import { Modal, Result, Button, Space, Typography, Divider } from 'antd';
import { ExclamationCircleOutlined, WalletOutlined } from '@ant-design/icons';

const { Text, Title } = Typography;

const InsufficientBalanceModal = ({ 
  isOpen, 
  onClose, 
  currentBalance, 
  requiredAmount, 
  onRecharge 
}) => {
  const shortfall = requiredAmount - currentBalance;

  return (
    <Modal
      title={null}
      open={isOpen}
      onCancel={onClose}
      footer={[
        <Button key="close" onClick={onClose}>
          稍后再试
        </Button>,
        <Button 
          key="recharge" 
          type="primary" 
          icon={<WalletOutlined />}
          onClick={onRecharge}
        >
          去充值
        </Button>
      ]}
      width={500}
      centered
    >
      <Result
        icon={<ExclamationCircleOutlined style={{ color: '#faad14' }} />}
        title={
          <Title level={4} style={{ color: '#faad14', margin: 0 }}>
            账户余额不足
          </Title>
        }
        subTitle="您的账户余额不足以支付此订单，请充值后再试"
      />
      
      <Divider />
      
      <Space direction="vertical" size="middle" style={{ width: '100%' }}>
        <div style={{ 
          background: '#f5f5f5', 
          padding: '16px', 
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          <Space direction="vertical" size="small">
            <Text type="secondary">订单金额</Text>
            <Text strong style={{ fontSize: '18px', color: '#ff4d4f' }}>
              ¥{requiredAmount.toFixed(2)}
            </Text>
          </Space>
        </div>
        
        <div style={{ 
          background: '#f5f5f5', 
          padding: '16px', 
          borderRadius: '8px',
          textAlign: 'center'
        }}>
          <Space direction="vertical" size="small">
            <Text type="secondary">当前余额</Text>
            <Text strong style={{ fontSize: '18px' }}>
              ¥{currentBalance.toFixed(2)}
            </Text>
          </Space>
        </div>
        
        <div style={{ 
          background: '#fff2f0', 
          padding: '16px', 
          borderRadius: '8px',
          textAlign: 'center',
          border: '1px solid #ffccc7'
        }}>
          <Space direction="vertical" size="small">
            <Text type="secondary">还需充值</Text>
            <Text strong style={{ fontSize: '18px', color: '#ff4d4f' }}>
              ¥{shortfall.toFixed(2)}
            </Text>
          </Space>
        </div>
      </Space>
    </Modal>
  );
};

export default InsufficientBalanceModal;
