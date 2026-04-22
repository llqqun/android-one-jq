import { defineStore } from 'pinia'
import config from '../utils/config.js'

export const useResumeStore = defineStore('resume', {
  state: () => ({
    schoolToken: config.schoolToken,
    schoolId: config.school_id,
    companyLoginInfo: null,
    deviceId: '',
    // deviceId: '0513201062000003180902854042382',
  }),
  getters: {
  },
  actions: {
    setCompanyLoginInfo(companyInfo) {
      this.companyLoginInfo = companyInfo
    },
    setDeviceId(id) {
      this.deviceId = id
    },
    async logout() {
      try {
        this.companyLoginInfo = null
      } catch (error) {
        console.error('退出登录失败:', error)
      } finally {
      }
    }
  }
})