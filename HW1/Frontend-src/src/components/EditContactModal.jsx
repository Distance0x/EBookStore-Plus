import React, { useState, useEffect } from 'react';
import { Modal, Form, Input, Button, message } from 'antd';

const { TextArea } = Input;

const EditContactModal = ({ isOpen, onClose, onSubmit, initialData }) => {
  const [form] = Form.useForm();
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 当模态框打开时，设置初始值
  useEffect(() => {
    if (isOpen && initialData) {
      form.setFieldsValue({
        address: initialData.address || '',
        phone: initialData.phone || ''
      });
    }
  }, [isOpen, initialData, form]);

  // 提交处理
  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      setIsSubmitting(true);
      
      await onSubmit({
        address: values.address.trim(),
        phone: values.phone.trim()
      });
      
      message.success('联系信息更新成功');
      handleClose();
    } catch (error) {
      if (error.errorFields) {
        // 表单验证错误，不需要额外处理
        return;
      }
      console.error('更新联系信息失败:', error);
      message.error(error.message || '更新失败，请重试');
    } finally {
      setIsSubmitting(false);
    }
  };

  // 关闭弹窗
  const handleClose = () => {
    form.resetFields();
    setIsSubmitting(false);
    onClose();
  };

  return (
    <Modal
      title="修改联系信息"
      open={isOpen}
      onCancel={handleClose}
      footer={[
        <Button key="cancel" onClick={handleClose} disabled={isSubmitting}>
          取消
        </Button>,
        <Button
          key="submit"
          type="primary"
          loading={isSubmitting}
          onClick={handleSubmit}
        >
          保存
        </Button>
      ]}
      width={500}
      destroyOnClose={true}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          address: initialData?.address || '',
          phone: initialData?.phone || ''
        }}
      >
        <Form.Item
          label="收货地址"
          name="address"
          rules={[
            { required: true, message: '收货地址不能为空' },
            { min: 5, message: '收货地址长度不能少于5个字符' }
          ]}
        >
          <TextArea
            placeholder="请输入详细的收货地址"
            rows={3}
            disabled={isSubmitting}
          />
        </Form.Item>
        
        <Form.Item
          label="联系电话"
          name="phone"
          rules={[
            { required: true, message: '联系电话不能为空' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' }
          ]}
        >
          <Input
            placeholder="请输入11位手机号码"
            disabled={isSubmitting}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};

export default EditContactModal;
