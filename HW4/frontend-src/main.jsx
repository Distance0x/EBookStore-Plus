import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import 'antd/dist/reset.css'

const root = createRoot(document.getElementById('root'))
root.render(
  // 注释掉 StrictMode 以避免开发环境下的双重渲染
  // <StrictMode>
    <App />
  // </StrictMode>
)
