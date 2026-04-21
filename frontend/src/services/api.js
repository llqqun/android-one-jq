import axios from 'axios'

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.PROD ? 'https://ynu.bysjy.com.cn' : '', // 基础URL，根据实际情况配置
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加认证信息等
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('API响应:', response)
    // 确保返回的数据结构一致
    if (response.data) {
      return response.data
    } else {
      // 处理304等没有data的响应
      return { code: 1, data: null, msg: '请求成功' }
    }
  },
  error => {
    console.error('API请求错误:', error)
    // 处理错误响应
    if (error.response) {
      console.error('响应错误:', error.response)
    } else if (error.request) {
      console.error('请求错误:', error.request)
    }
    return Promise.reject(error)
  }
)

// 简历相关API
export const resumeApi = {
  // 获取职位分类
  getPositionCategories: () => api.get('/api/position-categories'),
  
  // 获取学生简历列表
  getStudentResumes: (params) => api.get('/api/student-resumes', { params }),
  
  // 获取简历详情
  getResumeDetail: (id) => api.get(`/api/resumes/${id}`),
  
  // 更新简历状态
  updateResumeStatus: (id, status) => api.put(`/api/resumes/${id}/status`, { status }),
  
  // 获取职位列表
  getJobs: () => api.get('/api/jobs')
}

// 读卡器相关API
export const cardReaderApi = {
  // 连接设备
  connectDevice: (port) => api.post('/api/card-reader/connect', { port }),
  
  // 断开设备
  disconnectDevice: () => api.post('/api/card-reader/disconnect'),
  
  // 读取卡片
  readCard: () => api.post('/api/card-reader/read'),
  
  // 开始自动读卡
  startAutoRead: () => api.post('/api/card-reader/auto-read/start'),
  
  // 停止自动读卡
  stopAutoRead: () => api.post('/api/card-reader/auto-read/stop'),
  
  // 获取固件版本
  getFirmwareVersion: () => api.get('/api/card-reader/firmware-version')
}

// 登录相关API
export const loginApi = {
  // 获取学生登录二维码
  getQRCode: (params) => api.get('/machine/login/get_qr', { params }),
  // 检查学生登录状态
  checkLoginStatus: (params) => api.get('/machine/login/check', { params }),
  // 企业账号密码登录
  validateHRLogin: (data) => api.post('/machine/login/validate_hr', data),
  // 获取HR扫码登录二维码
  getHRQRCode: (params) => api.get('/machine/login/get_qr_hr', { params }),
  // 轮询检查HR扫码状态
  checkHRLoginStatus: (params) => api.get('/machine/login/check_hr', { params }),
  // 获取企业详情及职位信息
  getCompanyDetail: () => api.get('/machine/company/detail')
}

export default api