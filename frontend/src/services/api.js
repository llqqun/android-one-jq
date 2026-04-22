import axios from 'axios'
import config from '@/utils/config.js'

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.PROD ? config.apiUrl : '',
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
  /**
   * 获取企业详情及职位信息
   * @param {*} data 企业账号密码登录响应数据
   * @param {*} data.company_id 企业ID
   * @param {*} data.school_id 学校ID
   * @param {*} data.page 页码
   * @param {*} data.page_size 每页数量
   * 
   * @returns {*} data.company 企业详情
   * @returns {*} data.credit 职位分类
   * @returns {*} data.jobs 职位列表
   */
  getCompanyDetail: (data) => api.get('/machine/company/detail', { params: data }),
  
  /**
   * 获取职位申请列表
   * @param {*} data 请求参数
   * @param {*} data.device_id 设备ID
   * @param {*} data.company_id 企业ID
   * @param {*} data.school_id 学校ID
   * @param {*} data.publish_id 职位发布ID
   * @param {*} data.type 申请状态：pending(待处理), interested(有意向), unsuitable(不合适), proposed(拟录用)
   * @param {*} data.keywords 搜索关键词(姓名)
   * 
   * @returns {*} 申请列表数组
   */
  getJobApplyList: (data) => api.get('/machine/company/job_apply_list', { params: data }),
  
  /**
   * 获取职位和待处理的简历数量
   * @param {*} data 请求参数
   * @param {*} data.device_id 设备ID
   * @param {*} data.company_id 企业ID
   * @param {*} data.school_id 学校ID
   * 
   * @returns {*} data
   */
  getJobApplyCount: (data) => api.get('/machine/company/fair_job_list', { params: data }),
  
  /**
   * 获取简历详情
   * @param {*} data 请求参数
   * @param {*} data.apply_id 简历ID
   * 
   * @returns {*} 简历详情
   */
  getResumeDetail: (data) => api.get('/machine/company/job_apply_detail', { params: data }),
  
  /**
   * 获取学生简历列表
   * @param {*} data 请求参数
   * @param {*} data.school_id 学校ID
   * @param {*} data.student_id 学生ID
   * @param {*} data.student_key 学生密钥
   * 
   * @returns {*} 简历列表
   */
  getStudentResumes: (data) => api.get('/machine/resume/list', { params: data }),
  
  /**
   * 投递简历
   * @param {*} data 请求参数
   * @param {*} data.resume_id 简历ID
   * @param {*} data.job_ids 职位ID列表
   * @param {*} data.school_id 学校ID
   * @param {*} data.student_id 学生ID
   * @param {*} data.student_key 学生密钥
   * 
   * @returns {*} 投递结果
   */
  batchDelivery: (data) => api.post('/machine/resume/batch_delivery', data),

  /**
   * 更新简历状态
   * @param {*} data 请求参数
   * @param {*} data.apply_ids 简历ID
   * @param {*} data.company_id 企业ID
   * @param {*} data.status 状态：pending(待处理), interested(有意向), unsuitable(不合适), proposed(拟录用)
   * 
   * @returns {*} 更新结果
   */
  updateStatus: (data) => {
    if (data.status === 'unsuitable') {
      return api.post('/machine/company/apply_unconsider', data)
    } else if (data.status === 'interested') {
      return api.post('/machine/company/apply_pass', data)
    } else if (
      data.status === 'proposed'
    ) {
      return api.post('/machine/company/apply_intend', data)
    } else {
      return Promise.reject('状态错误')
    }
  }
}

export default api