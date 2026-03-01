import { useEffect, useContext } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { message } from 'antd'
import UserService from '../service/UserService'
import { UserContext } from '../utils/context'

/**
 * 管理员页面专用的Session检查Hook
 * 用于确保管理员页面在访问时都会检查session状态
 */
const useAdminSessionCheck = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, setUser, loading } = useContext(UserContext)

  // 检查session状态的函数
  const checkAdminSession = async () => {
    try {
      console.log('正在检查管理员session状态...')
      const response = await UserService.checkSession()
      
      if (response && response.isLoggedIn) {
        console.log('Session有效，用户已登录:', response.userAccount)
        
        // 检查是否是管理员
        if (response.user && response.user.role === 'admin') {
          console.log('管理员权限验证通过')
          
          // 更新用户数据
          if (!user || JSON.stringify(user) !== JSON.stringify(response.user)) {
            console.log('更新管理员用户上下文数据')
            setUser(response.user)
            localStorage.setItem('userProfile', JSON.stringify(response.user))
            localStorage.setItem('userAccount', response.userAccount)
            localStorage.setItem('isLoggedIn', 'true')
          }
          
          return true
        } else {
          console.log('用户不是管理员，权限不足')
          message.error('您没有管理员权限，无法访问管理页面')
          navigate('/login', { replace: true })
          return false
        }
      } else {
        console.log('Session无效或已过期')
        
        // 清除本地存储的过期数据
        localStorage.removeItem('userProfile')
        localStorage.removeItem('userAccount')
        localStorage.removeItem('isLoggedIn')
        setUser(null)
        
        message.warning('管理员会话已过期，请重新登录')
        navigate('/login', { replace: true })
        
        return false
      }
    } catch (error) {
      console.error('管理员Session检查失败:', error)
      
      // 网络错误或服务器错误时，清除本地数据并跳转
      localStorage.removeItem('userProfile')
      localStorage.removeItem('userAccount')
      localStorage.removeItem('isLoggedIn')
      setUser(null)
      
      message.error('网络连接异常，请重新登录')
      navigate('/login', { replace: true })
      
      return false
    }
  }

  // 每次路由变化时检查管理员session
  useEffect(() => {
    // 只在管理员路由中检查
    if (location.pathname.startsWith('/admin')) {
      // 如果UserContext还在loading，等待加载完成
      if (!loading) {
        checkAdminSession()
      }
    }
  }, [location.pathname, loading])

  return {
    isAdminAuthenticated: !!user && user.role === 'admin',
    checkAdminSession,
    loading
  }
}

export default useAdminSessionCheck
