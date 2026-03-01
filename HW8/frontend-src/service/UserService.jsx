import fetchApi from './api';

const UserService = {
  // 用户登录
  login: async (username, password) => {
    return fetchApi('/users/login', {
      method: 'POST',
      body: JSON.stringify({
        username,
        password
      })
    });
  },
  
  // 用户注册
  register: async (userData) => {
    return fetchApi('/users/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  },
  
  // 检查用户名是否可用
  checkUsername: async (username) => {
    return fetchApi(`/users/check-username/${username}`);
  },
  
  // 获取用户资料
  getUserProfile: async (account) => {
    return fetchApi(`/users/profile/${account}`);
  },
    // 更新用户资料
  updateUserProfile: async (account, profileData) => {
    return fetchApi(`/users/profile/${account}`, {
      method: 'PUT',
      body: JSON.stringify(profileData)
    });
  },
  
  // 检查会话状态
  checkSession: async () => {
    return fetchApi('/users/check-session');
  },
  
  // 退出登录
  logout: async () => {
    return fetchApi('/users/logout', {
      method: 'POST'
    });
  }
};

export default UserService;