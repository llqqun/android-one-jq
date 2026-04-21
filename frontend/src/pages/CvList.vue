<template>
  <div class="page-container">
    <div class="headers">
      <div class="header-left">
        <div class="company-logo">公司logo</div>
        <div class="company-name">长沙市云聘教育科技有限公司</div>
      </div>
      <div class="header-right">
        <div class="van-search">
          <input type="text" placeholder="搜索学生" class="van-search__input">
        </div>
        <img src="../assets/svgs/exit.svg" alt="exit" class="svg-icon" />
      </div>
    </div>

    <div class="content">
      <!-- 左侧职位分类 -->
      <div class="left-sidebar">
        <div class="sidebar-header">职位分类</div>
        <div class="category-list">
          <div class="category-item" v-for="category in positionCategories" :key="category.id">
            <div class="category-name">{{ category.name }}</div>
            <div class="category-count">{{ category.count }}</div>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="right-content">
        <div class="cv-list-container">
          <div class="tab-bar">
            <div class="tab-item" v-for="tab in tabs" :key="tab.value" :class="{ active: activeTab === tab.value }" 
              @click="switchTab(tab.value)">
              {{ tab.label }}
            </div>
          </div>

          <div class="cv-content">
            <!-- 左侧应聘人员列表 -->
            <div class="applicant-list">
              <div v-if="loading" class="loading-container">
                <div class="loading-spinner"></div>
                <div class="loading-text">加载中...</div>
              </div>
              <div v-else-if="error" class="error-container">
                <div class="error-text">{{ error }}</div>
                <div class="retry-btn" @click="resumeStore.fetchStudentResumes({ status: activeTab })">重试</div>
              </div>
              <div v-else>
                <div class="applicant-item" v-for="resume in studentResumes" :key="resume.id" 
                  :class="{ active: selectedResume && selectedResume.id === resume.id }" 
                  @click="selectResume(resume)">
                  <div class="applicant-header">
                    <div class="avatar">{{ resume.name.charAt(0) }}</div>
                    <div class="applicant-basic">
                      <div class="name-info">
                        <span class="name">{{ resume.name }}</span>
                        <div class="gender">
                          <img v-if="resume.gender === 'male'" src="../assets/svgs/man.svg" alt="male" class="svg-icon" />
                          <img v-else src="../assets/svgs/woman.svg" alt="female" class="svg-icon" />
                        </div>
                        <span class="age">{{ resume.age }}岁</span>
                        <span class="apply-time">{{ resume.applyTime }}</span>
                      </div>
                      <div class="intention-salary">
                        <span class="intention-title">求职意向：</span>
                        <span class="intention">{{ resume.intention }}</span>
                        <span class="salary">{{ resume.salary }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="work-experience">
                    <div class="exp-item" v-for="(exp, index) in resume.workExperience.slice(0, 2)" :key="index">
                      <div class="exp-bullet">
                        <img src="../assets/svgs/job.svg" alt="job" class="svg-icon" />
                      </div>
                      <div class="exp-content">
                        <div class="exp-period">{{ exp.period }}</div>
                        <div class="exp-info">{{ exp.company }}</div>
                        <div class="exp-position">{{ exp.position }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 右侧简历详情 -->
            <div class="resume-detail" v-if="selectedResume">
              <div class="cv-detail">
                <div class="detail-header">
                  <div class="avatar-large">{{ selectedResume.name.charAt(0) }}</div>
                  <div class="detail-basic">
                    <div class="name-age">{{ selectedResume.name }} {{ selectedResume.age }}岁</div>
                    <div class="intention-salary">
                      <span class="intention">{{ selectedResume.intention }}</span>
                      <span class="salary">{{ selectedResume.salary }}</span>
                    </div>
                    <div class="contact-info">
                      <span class="phone">{{ selectedResume.phone }}</span>
                      <span class="email">{{ selectedResume.email }}</span>
                    </div>
                  </div>
                </div>

                <!-- 教育经历 -->
                <div class="section">
                  <div class="section-title">教育经历</div>
                  <div class="section-content">
                    <div class="education-item" v-for="(edu, index) in selectedResume.education" :key="index">
                      <div class="edu-period">{{ edu.period }}</div>
                      <div class="edu-info">
                        <div class="edu-school">{{ edu.school }}</div>
                        <div class="edu-detail">{{ edu.degree }}·{{ edu.major }}</div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 自我描述 -->
                <div class="section">
                  <div class="section-title">自我描述</div>
                  <div class="section-content">
                    <div class="self-intro">{{ selectedResume.selfIntro }}</div>
                  </div>
                </div>

                <!-- 工作经历 -->
                <div class="section">
                  <div class="section-title">工作经历</div>
                  <div class="section-content">
                    <div class="work-item" v-for="(work, index) in selectedResume.workExperience" :key="index">
                      <div class="work-period">{{ work.period }}</div>
                      <div class="work-info">
                        <div class="work-company">{{ work.company }}</div>
                        <div v-if="work.position" class="work-position">{{ work.position }}</div>
                        <div v-if="work.description" class="work-description">{{ work.description }}</div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 在校经历 -->
                <div class="section">
                  <div class="section-title">在校经历</div>
                  <div class="section-content">
                    <div class="school-item" v-for="(school, index) in selectedResume.schoolExperience" :key="index">
                      <div class="school-period">{{ school.period }}</div>
                      <div class="school-description">{{ school.description }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <div class="btn" @click="handleStatusChange(selectedResume, '不合适')">不合适</div>
                <div class="btn btn-blue" @click="handleStatusChange(selectedResume, '有意向')">有意向</div>
                <div class="btn btn-blue" @click="handleStatusChange(selectedResume, '拟录用')">拟录用</div>
              </div>
            </div>
            <div class="resume-detail empty" v-else>
              <div class="empty-text">请选择一位应聘人员查看详情</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useResumeStore } from '../store/resume'

const resumeStore = useResumeStore()

const positionCategories = resumeStore.positionCategories
const studentResumes = resumeStore.studentResumes
const tabs = resumeStore.tabs
const activeTab = resumeStore.activeTab
const selectedResume = computed(() => resumeStore.selectedResume)
const loading = computed(() => resumeStore.loading)
const error = computed(() => resumeStore.error)

const selectResume = (resume) => {
  resumeStore.selectResume(resume)
}

const switchTab = (value) => {
  resumeStore.switchTab(value)
  // 切换标签时重新获取对应状态的简历
  resumeStore.fetchStudentResumes({ status: value })
}

const handleStatusChange = (resume, status) => {
  resumeStore.updateResumeStatus(resume, status)
}

// 页面加载时获取数据
onMounted(() => {
  resumeStore.fetchPositionCategories()
  resumeStore.fetchStudentResumes({ status: activeTab })
})
</script>

<style lang="scss" scoped>
@import '../assets/mixins.scss';
.page-container {
  background-color: #fff;
  @include flex-fun(column, flex-start, flex-start);
  min-height: 100vh;
}

.headers {
  width: 100%;
  @include flex-fun(row, flex-start, center);
  padding: 0 20px;
  background-color: #fff;
  height: 60px;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  .header-left {
    @include flex-fun(row, flex-start, center);
    flex: 1;
  }

  .header-right {
    @include flex-fun(row, flex-start, center);
  }
}

.content {
  width: 100%;
  flex: 1;
}

.svg-icon {
  margin-left: 20px;
  width: 20px;
  height: 20px;
}

.company-name {
  font-size: 16px;
  position: relative;
  padding-left: 48px;
}

.company-name::before {
  content: '';
  position: absolute;
  left: 24px;
  top: 50%;
  transform: translateY(-50%);
  background-color: #dddddd;
  width: 1px;
  height: 14px;
}

.content {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  flex: 1;
  overflow: hidden;
}

.left-sidebar {
  width: 180px;
  background-color: #ffffff;
  border-top-right-radius: 12px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
}

.sidebar-header {
  font-size: 14px;
  font-weight: 600;
  color: #333333;
  padding: 16px;
  border-bottom: 1px solid #e8ecf4;
  background-color: #f8f9fa;
}

.category-list {
  flex: 1;
  overflow-y: auto;
}

.category-item {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  font-size: 13px;
  color: #555555;
  border-bottom: 1px solid #e8ecf4;
  transition: background-color 0.3s;
  cursor: pointer;
}

.category-item:hover {
  background-color: #f7fbff;
}

.category-count {
  font-size: 12px;
  color: #999999;
  background-color: #f0f0f0;
  padding: 2px 8px;
  border-radius: 10px;
}

.right-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #E2F1FF;
  border-top-left-radius: 12px;
  background-color: #F7FBFF;
  padding: 10px;
  margin-left: 10px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .left-sidebar {
    width: 150px;
  }
  
  .applicant-list {
    width: 280px;
  }
}

@media (max-width: 992px) {
  .content {
    flex-direction: column;
  }
  
  .left-sidebar {
    width: 100%;
    border-top-right-radius: 0;
    border-bottom-left-radius: 12px;
    border-bottom-right-radius: 12px;
    margin-bottom: 10px;
  }
  
  .right-content {
    margin-left: 0;
    border-top-left-radius: 12px;
    border-top-right-radius: 12px;
  }
  
  .cv-content {
    flex-direction: column;
    height: auto;
  }
  
  .applicant-list {
    width: 100%;
    height: 300px;
    margin-bottom: 10px;
  }
  
  .resume-detail {
    width: 100%;
    height: 400px;
  }
}

.cv-list-container {
  background-color: #ffffff;
  padding: 10px;
  height: 100%;
}

.tab-bar {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  width: 100%;
  padding: 0;
  background-color: #ffffff;
  border-bottom: 1px solid #EEEEEE;
}

.tab-item {
  padding: 6px 18px;
  font-size: 14px;
  color: #333333;
  background-color: #fafafa;
  border-bottom: 1px solid #e8ecf4;
  position: relative;
  border: 1px solid #EEEEEE;
  margin-bottom: -1px;
}

.tab-item+.tab-item {
  margin-left: 6px;
}

.tab-item.active {
  background-color: #ffffff;
  color: #0180ff;
  font-weight: 500;
  border-bottom: none;
}

.cv-content {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  overflow: hidden;
  background-color: #ffffff;
  margin-top: 10px;
  height: 620px;
}

.applicant-list {
  width: 330px;
  flex-shrink: 0;
  background-color: #ffffff;
  padding-right: 14px;
  height: 100%;
  overflow-y: auto;
}

.applicant-item {
  padding: 12px;
  cursor: pointer;
  background-color: #F7FBFF;
  border: 1px solid #E2F1FF;
  border-radius: 4px;
  position: relative;
}

.applicant-item::after {
  content: '';
  position: absolute;
  right: -3px;
  top: 50%;
  transform: rotate(45deg) translateY(-50%);
  width: 10px;
  height: 10px;
  border: 1px solid #3BA0FE;
  border-left: none;
  border-bottom: none;
  display: none;
}

.applicant-item.active::after {
  display: block;
}

.applicant-item+.applicant-item {
  margin-top: 10px;
}

.applicant-item.active {
  border-color: #3BA0FE;
}

.applicant-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  background-color: #0080ff;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  font-weight: 600;
  margin-right: 12px;
}

.applicant-basic {
  flex: 1;
}

.name-info {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  margin-bottom: 4px;
}

.name {
  font-weight: 600;
  color: #333333;
}

.gender {
  font-size: 0px;
}

.gender .svg-icon {
  width: 12px;
  height: 12px;
}

.age {
  color: #999999;
  font-size: 12px;
}

.apply-time {
  color: #999999;
  font-size: 12px;
  margin-left: auto;
}

.intention-salary {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 12px;
  font-size: 10px;
  color: #555555;
}

.salary {
  padding-left: 5px;
  border-left: 1px solid #DADADA;
}

.work-experience {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exp-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 12px;
  color: #555555;
}

.exp-bullet {
  margin-right: 4px;
}

.exp-bullet .svg-icon {
  width: 10px;
  height: 10px;
  margin-left: 0;
}

.exp-content {
  flex: 1;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  font-size: 10px;
}

.exp-period {
  color: #555555;
  margin-bottom: 2px;
}

.resume-detail {
  flex: 1;
  padding: 10px;
  height: 100%;
  box-sizing: border-box;
  background-color: #ffffff;
  border: 1px solid #EEEEEE;
  border-radius: 4px;
  position: relative;
  padding-bottom: 0px;
}

.cv-detail {
  height: 100%;
  overflow-y: auto;
  padding-bottom: 60px;
  box-sizing: border-box;
}

.resume-detail.empty {
  display: flex;
  justify-content: center;
  align-items: center;
}

.empty-text {
  font-size: 14px;
  color: #999999;
}

.detail-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8ecf4;
}

.avatar-large {
  width: 80px;
  height: 80px;
  background-color: #0080ff;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 32px;
  font-weight: 600;
  margin-right: 16px;
}

.detail-basic {
  flex: 1;
}

.name-age {
  font-size: 18px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 8px;
}

.intention-salary {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  margin-bottom: 8px;
}

.intention {
  color: #555555;
}

.salary {
  color: #0080ff;
  font-weight: 500;
}

.contact-info {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999999;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 12px;
}

.section-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.education-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 14px;
}

.edu-period {
  width: 180px;
  color: #999999;
  flex-shrink: 0;
}

.edu-info {
  flex: 1;
  color: #555555;
}

.edu-school {
  font-weight: 500;
  margin-bottom: 4px;
}

.edu-detail {
  font-size: 13px;
}

.self-intro {
  font-size: 14px;
  color: #555555;
  line-height: 1.5;
}

.work-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 14px;
}

.work-period {
  width: 180px;
  color: #999999;
  flex-shrink: 0;
}

.work-info {
  flex: 1;
  color: #555555;
}

.work-company {
  font-weight: 500;
  margin-bottom: 4px;
}

.work-position {
  font-size: 13px;
  margin-bottom: 4px;
}

.work-description {
  font-size: 13px;
  line-height: 1.4;
}

.school-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 14px;
}

.school-period {
  width: 180px;
  color: #999999;
  flex-shrink: 0;
}

.school-description {
  flex: 1;
  color: #555555;
  font-size: 13px;
  line-height: 1.4;
}

.action-buttons {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  background-color: #fff;
  padding: 10px 0;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.btn {
  background-color: #ffffff;
  color: #555555;
  border: 1px solid #EEEEEE;
  padding: 8px 24px;
  border-radius: 4px;
  font-size: 14px;
  margin-right: 10px;
}

.btn-blue {
  background-color: #0080ff;
  color: #ffffff;
  padding: 8px 24px;
  border-radius: 4px;
  font-size: 14px;
}

/* 加载状态样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  width: 100%;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #0080ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 14px;
  color: #666666;
}

/* 错误状态样式 */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  width: 100%;
  padding: 20px;
  text-align: center;
}

.error-text {
  font-size: 14px;
  color: #ff4d4f;
  margin-bottom: 16px;
}

.retry-btn {
  background-color: #0080ff;
  color: #ffffff;
  padding: 8px 24px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.retry-btn:hover {
  background-color: #0066cc;
}
</style>