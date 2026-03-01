import { createContext, useState, useEffect } from 'react'
import UserService from '../service/UserService'

export const UserContext = createContext()

export const UserProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedData = localStorage.getItem('userProfile')
    return savedData ? JSON.parse(savedData) : null
  })
  const [loading, setLoading] = useState(true)

  // 检查session状态
  const checkSession = async () => {
    try {
      setLoading(true)
      console.log('正在检查session状态...')
      const response = await UserService.checkSession()
      
      if (response && response.isLoggedIn) {
        console.log('Session有效，用户已登录:', response.userAccount)
        
        // 更新用户数据
        if (response.user) {
          setUser(response.user)
          localStorage.setItem('userProfile', JSON.stringify(response.user))
          localStorage.setItem('userAccount', response.userAccount)
          localStorage.setItem('isLoggedIn', 'true')
        }
        
        return true
      } else {
        console.log('Session无效或已过期')
        
        // 清除本地存储的过期数据
        localStorage.removeItem('userProfile')
        localStorage.removeItem('userAccount')
        localStorage.removeItem('isLoggedIn')
        setUser(null)
        
        return false
      }
    } catch (error) {
      console.error('Session检查失败:', error)
      
      // 网络错误或服务器错误时，清除本地数据
      localStorage.removeItem('userProfile')
      localStorage.removeItem('userAccount')
      localStorage.removeItem('isLoggedIn')
      setUser(null)
      
      return false
    } finally {
      setLoading(false)
    }
  }

  // 应用启动时检查session
  useEffect(() => {
    // 如果localStorage中有用户数据，则检查session
    const savedData = localStorage.getItem('userProfile')
    if (savedData) {
      checkSession()
    } else {
      setLoading(false)
    }
  }, [])

  return (
    // 指定要传递给消费组件的数据或函数
    <UserContext.Provider value={{ user, setUser, loading, checkSession }}>
      {children}
    </UserContext.Provider>
    // {children} 是一个特殊的prop，它代表了在Provider标签内部的所有内容
  )
}

export default UserProvider;