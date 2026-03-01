import { useState, useRef, useEffect, useContext } from 'react';
import { 
  Bubble, 
  Sender,
  useXAgent,
  useXChat,
} from '@ant-design/x';
import { 
  Typography, 
  Space, 
  Button, 
  message,
  Flex,
} from 'antd';
import { 
  RobotOutlined, 
  UserOutlined, 
  ClearOutlined,
  StopOutlined,
} from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import { UserContext } from '../../utils/context';
import { sendMessageStream, generateSessionId } from '../../service/chatService';

const { Title, Text } = Typography;

// 三个点加载动画组件
const LoadingDots = () => {
  return (
    <Space>
      <span className="loading-dot" style={{
        display: 'inline-block',
        width: 8,
        height: 8,
        borderRadius: '50%',
        backgroundColor: '#52c41a',
        animation: 'dotPulse 1.4s ease-in-out infinite',
        animationDelay: '0s',
      }} />
      <span className="loading-dot" style={{
        display: 'inline-block',
        width: 8,
        height: 8,
        borderRadius: '50%',
        backgroundColor: '#52c41a',
        animation: 'dotPulse 1.4s ease-in-out infinite',
        animationDelay: '0.2s',
      }} />
      <span className="loading-dot" style={{
        display: 'inline-block',
        width: 8,
        height: 8,
        borderRadius: '50%',
        backgroundColor: '#52c41a',
        animation: 'dotPulse 1.4s ease-in-out infinite',
        animationDelay: '0.4s',
      }} />
      <style>{`
        @keyframes dotPulse {
          0%, 80%, 100% {
            transform: scale(0.6);
            opacity: 0.5;
          }
          40% {
            transform: scale(1);
            opacity: 1;
          }
        }
      `}</style>
    </Space>
  );
};

// 内联样式
const styles = {
  chatContainer: {
    display: 'flex',
    flexDirection: 'column',
    height: 'calc(100vh - 160px)',
    maxWidth: '900px',
    margin: '0 auto',
    background: '#fff',
    borderRadius: '12px',
    overflow: 'hidden',
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.08)',
  },
  chatHeader: {
    padding: '16px 24px',
    borderBottom: '1px solid #f0f0f0',
    background: 'linear-gradient(135deg, #f6ffed 0%, #e6f7ff 100%)',
  },
  chatMessages: {
    flex: 1,
    overflowY: 'auto',
    padding: '24px',
    background: '#fafafa',
  },
  chatInput: {
    padding: '16px 24px',
    borderTop: '1px solid #f0f0f0',
    background: '#fff',
  },
  chatWelcome: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100%',
    textAlign: 'center',
    color: '#666',
    padding: '40px',
  },
  welcomeIcon: {
    fontSize: '64px',
    color: '#52c41a',
    marginBottom: '24px',
  },
  welcomeTips: {
    listStyle: 'none',
    padding: 0,
    marginTop: '16px',
    textAlign: 'left',
  },
  welcomeTipItem: {
    padding: '8px 0',
    color: '#666',
    fontSize: '14px',
  },
  markdownContent: {
    lineHeight: '1.6',
    fontSize: '14px',
  },
};

// 角色配置 - 使用 loadingRender 显示三个点动画
const roles = {
  user: {
    placement: 'end',
    avatar: { icon: <UserOutlined />, style: { background: '#1890ff' } },
  },
  assistant: {
    placement: 'start',
    avatar: { icon: <RobotOutlined />, style: { background: '#52c41a' } },
    typing: { step: 5, interval: 20 },
    loadingRender: () => <LoadingDots />,
  },
};

const ChatPage = () => {
  const { user } = useContext(UserContext);
  const [sessionId] = useState(() => generateSessionId());
  const [inputValue, setInputValue] = useState('');
  const abortControllerRef = useRef(null);
  const messagesEndRef = useRef(null);

  // 使用 useXAgent 创建智能体
  const [agent] = useXAgent({
    request: async (info, callbacks) => {
      const { message: userMessage } = info;
      const { onSuccess, onError, onUpdate } = callbacks;
      
      // 创建新的 AbortController
      abortControllerRef.current = new AbortController();
      
      let fullContent = '';
      
      try {
        await sendMessageStream(
          userMessage,
          sessionId,
          (chunk) => {
            fullContent += chunk;
            onUpdate(fullContent);
          },
          abortControllerRef.current.signal
        );
        
        onSuccess(fullContent || '抱歉，我没有收到有效的响应。');
      } catch (error) {
        if (error.name === 'AbortError') {
          onSuccess(fullContent || '对话已停止');
        } else {
          console.error('Chat error:', error);
          onError(error);
        }
      }
    },
  });

  // 使用 useXChat 管理对话
  const { 
    messages, 
    onRequest, 
    setMessages,
  } = useXChat({ agent });

  // 滚动到底部
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 发送消息
  const handleSend = (text) => {
    // 如果正在加载中，禁止发送
    if (isLoading) {
      return;
    }
    
    if (!text.trim()) {
      message.warning('请输入消息');
      return;
    }
    
    setInputValue('');
    onRequest(text);
  };

  // 停止生成
  const handleStop = () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
      abortControllerRef.current = null;
    }
  };

  // 清空对话
  const handleClear = () => {
    setMessages([]);
    message.success('对话已清空');
  };

  // 检查是否正在加载
  const isLoading = messages.some(msg => msg.status === 'loading');

  // 渲染消息列表
  const renderMessages = () => {
    if (messages.length === 0) {
      return (
        <div style={styles.chatWelcome}>
          <RobotOutlined style={styles.welcomeIcon} />
          <Title level={3}>欢迎使用 AI 助手</Title>
          <Text type="secondary">
            我是您的智能购书助手，可以帮您：
          </Text>
          <ul style={styles.welcomeTips}>
            <li style={styles.welcomeTipItem}>🔍 查找特定类型的书籍</li>
            <li style={styles.welcomeTipItem}>💡 解答购书相关问题</li>
          </ul>
        </div>
      );
    }

    return messages.map((msg) => {
      const isUser = msg.status === 'local';
      const isLoading = msg.status === 'loading';
      const isStreaming = msg.status === 'loading' && msg.message;
      
      // 用户消息直接显示，AI 消息使用 Markdown 渲染
      const content = isUser ? msg.message : (
        <div style={styles.markdownContent}>
          <ReactMarkdown>{msg.message || ''}</ReactMarkdown>
        </div>
      );
      
      return (
        <Bubble
          key={msg.id}
          role={isUser ? 'user' : 'assistant'}
          content={content}
          loading={isLoading && !msg.message}
          typing={isStreaming ? { step: 2, interval: 50 } : false}
          {...roles[isUser ? 'user' : 'assistant']}
        />
      );
    });
  };

  return (
    <div style={styles.chatContainer}>
      {/* 头部 */}
      <div style={styles.chatHeader}>
        <Flex justify="space-between" align="center">
          <Space>
            <RobotOutlined style={{ fontSize: 24, color: '#52c41a' }} />
            <div>
              <Title level={4} style={{ margin: 0 }}>AI 购书助手</Title>
              <Text type="secondary" style={{ fontSize: 12 }}>
                {user?.nickname || user?.username || '用户'}，有什么可以帮您？
              </Text>
            </div>
          </Space>
          <Space>
            {isLoading && (
              <Button 
                icon={<StopOutlined />} 
                onClick={handleStop}
                danger
              >
                停止生成
              </Button>
            )}
            <Button 
              icon={<ClearOutlined />} 
              onClick={handleClear}
              disabled={messages.length === 0}
            >
              清空对话
            </Button>
          </Space>
        </Flex>
      </div>

      {/* 消息区域 */}
      <div style={styles.chatMessages}>
        <Flex vertical gap="middle">
          {renderMessages()}
          <div ref={messagesEndRef} />
        </Flex>
      </div>

      {/* 输入区域 */}
      <div style={styles.chatInput}>
        <Sender
          value={inputValue}
          onChange={setInputValue}
          onSubmit={handleSend}
          placeholder="输入您的问题，按 Enter 发送..."
          loading={isLoading}
          disabled={isLoading}
        />
      </div>
    </div>
  );
};

export default ChatPage;
