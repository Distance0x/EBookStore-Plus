import { useEffect, useContext, useCallback, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { message } from 'antd'
import UserService from '../service/UserService'
import { UserContext } from '../utils/context'

/**
 * 自定义Hook - 用于检查session状态和自动跳转
 * @param {boolean} checkOnMount - 是否在组件挂载时检查session（默认为true）
 * @returns {Object} { isAuthenticated, checkSession }
 */
const useSessionCheck = (checkOnMount = true) => {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, setUser } = useContext(UserContext)
  const checkingRef = useRef(false) // 防止重复检查
  const userRef = useRef(user) // 使用 ref 保存最新的 user 值
  const setUserRef = useRef(setUser) // 使用 ref 保存最新的 setUser 函数
  
  // 保持 ref 同步
  useEffect(() => {
    userRef.current = user
    setUserRef.current = setUser
  }, [user, setUser])
  
  // 使用 useCallback 确保 checkSession 函数引用稳定
  const checkSession = useCallback(async () => {
    // 如果正在检查，直接返回
    if (checkingRef.current) {
      console.log('Session检查已在进行中，跳过重复请求')
      return
    }
    
    checkingRef.current = true
    try {
      console.log('正在检查session状态...')
      const response = await UserService.checkSession()
      
      if (response && response.isLoggedIn) {
        console.log('Session有效，用户已登录:', response.userAccount)
        
        // 使用 ref 获取最新的 user 值
        const currentUser = userRef.current
        // 只有当用户数据发生变化时才更新用户上下文
        if (response.user && (!currentUser || JSON.stringify(currentUser) !== JSON.stringify(response.user))) {
          console.log('更新用户上下文数据')
          setUserRef.current(response.user)
          localStorage.setItem('userProfile', JSON.stringify(response.user))
          localStorage.setItem('userAccount', response.userAccount)
          localStorage.setItem('isLoggedIn', 'true')
        }
        
        return true
      } else {
        console.log('Session无效或已过期')
        
        // 使用 ref 获取最新的 user 值
        const currentUser = userRef.current
        // 只有当用户还存在时才清除数据
        if (currentUser) {
          // 清除本地存储的过期数据
          localStorage.removeItem('userProfile')
          localStorage.removeItem('userAccount')
          localStorage.removeItem('isLoggedIn')
          setUserRef.current(null)
        }
        
        // 如果当前不在登录或注册页面，则跳转到登录页面
        if (location.pathname !== '/login' && location.pathname !== '/register') {
          message.warning('会话已过期，请重新登录')
          navigate('/login', { replace: true })
        }
        
        return false
      }
    } catch (error) {
      console.error('Session检查失败:', error)
      
      // 使用 ref 获取最新的 user 值
      const currentUser = userRef.current
      // 只有当用户还存在时才清除数据
      if (currentUser) {
        // 网络错误或服务器错误时，清除本地数据并跳转
        localStorage.removeItem('userProfile')
        localStorage.removeItem('userAccount')
        localStorage.removeItem('isLoggedIn')
        setUserRef.current(null)
      }
      
      if (location.pathname !== '/login' && location.pathname !== '/register') {
        message.error('网络连接异常，请重新登录')
        navigate('/login', { replace: true })
      }
      
      return false
    } finally {
      // 重置检查标志
      checkingRef.current = false
    }
  }, [navigate, location.pathname])
  
  // 组件挂载时自动检查session
  useEffect(() => {
    if (checkOnMount) {
      // 只在受保护的路由中检查session
      const protectedRoutes = ['/home', '/cart', '/orders', '/checkout', '/books', '/admin', '/statistics']
      const isProtectedRoute = protectedRoutes.some(route => 
        location.pathname.startsWith(route) || location.pathname === '/'
      )
      
      if (isProtectedRoute) {
        checkSession()
      }
    }
  }, [checkOnMount, location.pathname, checkSession])

  return {
    isAuthenticated: !!user,
    checkSession
  }
}

export default useSessionCheck
