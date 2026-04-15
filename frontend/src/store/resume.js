import { defineStore } from 'pinia'

export const useResumeStore = defineStore('resume', {
  state: () => ({
    positionCategories: [
      { id: 1, name: '数据工程师', count: 32 },
      { id: 2, name: '前端工程师', count: 28 },
      { id: 3, name: '心脑血管科', count: 15 },
      { id: 4, name: '会计', count: 8 },
      { id: 5, name: '产品经理', count: 12 },
      { id: 6, name: '测试工程师', count: 10 }
    ],
    studentResumes: [
      {
        id: 1,
        name: '云小帅',
        age: 21,
        avatar: '',
        gender: 'male',
        intention: 'Android开发工程师',
        salary: '10K-15K',
        education: [
          { period: '2021.09 - 2025.07', school: '湖南师范大学', degree: '硕士', major: '国际关系' },
          { period: '2017.09 - 2021.07', school: '湘潭大学', degree: '本科生', major: '工商管理' }
        ],
        selfIntro: '本人性格开朗活泼，工作认真负责，具有较好的沟通能力，善于与人沟通，不仅能独立人做好工作，能与同事、领导、客户不期而遇。因母亲瘫痪，',
        workExperience: [
          {
            period: '2020.06 - 2020.07',
            company: '主要负责记录该学院学生学习情况，督促学生学习，帮助学生学习，给学生答疑解惑，并组织长知识竞赛，了解学生情况。'
          },
          {
            period: '2021.07 - 2020.09',
            company: '广汽菲亚特克莱斯勒汽车有限公司',
            position: '推销员',
            description: '主要负责汽车展厅的客户接待，产品讲解，材料申报以及产品试验。'
          }
        ],
        schoolExperience: [
          {
            period: '2020.06 - 2020.07',
            description: '1. 作为办公室成员负责办公室的日常管理，协助学院老师开展学生管理工作，帮助老师打印、整理资料。\n2. 作为学习委员，协助学习，带领学习小组，提高学习氛围，及时向老师反映学习情况，有较好的协调工作能力。'
          },
          {
            period: '2020.06 - 2020.07',
            description: '1. 作为办公室成员负责办公室的日常管理，协助学院老师开展学生管理工作，帮助老师打印、整理资料。\n2. 作为学习委员，协助学习，带领学习小组，提高学习氛围，及时向老师反映学习情况，有较好的协调工作能力。'
          }
        ],
        status: '已处理',
        applyTime: '2025-03-28 10:15',
        phone: '15****55566',
        email: '131*****107@qq.com'
      },
      {
        id: 2,
        name: '云小帅',
        age: 21,
        avatar: '',
        intention: 'Android开发工程师',
        salary: '10K-15K',
        education: [],
        selfIntro: '',
        workExperience: [],
        schoolExperience: [],
        status: '已处理',
        applyTime: '2025-03-28 10:15'
      },
      {
        id: 3,
        name: '云小帅',
        age: 21,
        avatar: '',
        intention: 'Android开发工程师',
        salary: '10K-15K',
        education: [],
        selfIntro: '',
        workExperience: [],
        schoolExperience: [],
        status: '已处理',
        applyTime: '2025-03-28 10:15'
      }
    ],
    jobs: [
      {
        title: '中高级产品经理,很长很长很长很长很长的标题很长很长很长很长很长的标题很长很长很长很长很长的标题',
        count: 3,
        degree: '本科及以上',
        city: '衡阳市',
        salary: '10K-15K/月',
        isChecked: false,
      },
      { title: '英语老师', count: 3, degree: '本科及以上', city: '衡阳市', salary: '10K-15K/月', isChecked: false },
      { title: '中高级产品经理', count: 3, degree: '本科及以上', city: '衡阳市', salary: '10K-15K/月', isChecked: false },
      { title: '英语老师', count: 3, degree: '本科及以上', city: '衡阳市', salary: '10K-15K/月', isChecked: false }
    ],
    cvList: [
      { id: 1, name: '产品经理', isDefault: true },
      { id: 2, name: '测试工程师', isDefault: false },
      { id: 3, name: '研发工程师', isDefault: false },
      { id: 4, name: '前端工程师', isDefault: false }
    ],
    selectedResume: null,
    activeTab: '1',
    tabs: [
      { label: '待处理', value: '1' },
      { label: '有意向', value: '2' },
      { label: '不合适', value: '3' },
      { label: '初试用', value: '4' }
    ]
  }),
  getters: {
    getSelectedResume: (state) => state.selectedResume,
    getActiveTab: (state) => state.activeTab,
    getTabs: (state) => state.tabs,
    getStudentResumes: (state) => state.studentResumes,
    getPositionCategories: (state) => state.positionCategories,
    getJobs: (state) => state.jobs,
    getCvList: (state) => state.cvList,
  },
  actions: {
    selectResume(resume) {
      this.selectedResume = resume
    },
    switchTab(value) {
      this.activeTab = value
    },
    updateResumeStatus(resume, status) {
      resume.status = status
    }
  }
})