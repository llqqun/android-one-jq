<template>
  <div class="page-container">
    <div class="headers">
      <div class="header-left">
        <div class="company-logo">
          <img :src="companyInfo?.logo_url" alt="公司logo" class="logo-image" />
        </div>
        <div class="company-name">{{ companyInfo?.company_name }}</div>
      </div>
      <div class="header-right">
        <!-- 页面数据刷新按钮 -->
        <div>
          <van-icon name="replay" size="24" @click="pageInit" />
        </div>
        <div class="van-search">
          <van-search v-model="searchText" placeholder="搜索学生" @search="handleSearch"/>
        </div>
        <img src="../assets/svgs/exit.svg" alt="exit" class="svg-icon" @click="logout" />
      </div>
    </div>

    <div class="content">
      <!-- 左侧职位分类 -->
      <div class="left-sidebar">
        <div class="sidebar-header">招聘中的职位</div>
        <div v-if="positionCategories.length > 0" class="category-list">
          <div
            class="category-item"
            v-for="category in positionCategories"
            :key="category.publish_id"
            :class="{ active: category.publish_id === currentCategory?.publish_id }"
            @click="switchCategory(category)">
            <div class="category-name">{{ category.job_name }}</div>
            <div v-if="category.pending_count" class="category-count">
              {{ category.pending_count }}
            </div>
          </div>
        </div>
        <div v-else class="no-category">暂无职位分类</div>
      </div>

      <!-- 右侧内容 -->
      <div class="right-content">
        <div class="cv-list-container">
          <div class="tab-bar">
            <div
              class="tab-item"
              v-for="tab in tabs"
              :key="tab.value"
              :class="{ active: activeTab === tab.value }"
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
              <div v-else-if="studentResumes.length === 0" class="no-applicant">
                <div class="no-applicant">暂无简历申请</div>
              </div>
              <div v-else>
                <div
                  class="applicant-item"
                  v-for="resume in studentResumes"
                  :key="resume.apply_id"
                  :class="{ active: selectedResume && selectedResume.apply_id === resume.apply_id }"
                  @click="selectResume(resume)">
                  <div class="applicant-header">
                    <div class="avatar">
                      <img :src="resume.head_url" alt="avatar" class="avatar-image" />
                    </div>
                    <div class="applicant-basic">
                      <div class="name-info">
                        <span class="name">{{ resume.user_name }}</span>
                        <div class="gender">
                          <img
                            v-if="resume.sex === '男'"
                            src="../assets/svgs/man.svg"
                            alt="男"
                            class="svg-icon" />
                          <img v-else src="../assets/svgs/woman.svg" alt="女" class="svg-icon" />
                        </div>
                        <span class="age">{{ resume.age }}岁</span>
                        <span class="apply-time">{{ resume.apply_time }}</span>
                      </div>
                      <div class="intention-salary">
                        <span class="intention-title">求职意向：</span>
                        <span class="intention">{{ resume.expect_job }}</span>
                        <span class="salary">{{ resume.expect_city }}</span>
                        <span class="salary"
                          >{{ resume.expected_salary_min }}k -
                          {{ resume.expected_salary_max }}k</span
                        >
                      </div>
                    </div>
                  </div>
                  <div class="work-experience">
                    <div class="exp-item" v-for="(exp, index) in [resume.last_work]" :key="index">
                      <div class="exp-bullet">
                        <img src="../assets/svgs/job.svg" alt="job" class="svg-icon" />
                      </div>
                      <div class="exp-content">
                        <div class="exp-period">{{ exp.begin_date }}</div>
                        <div class="exp-info">{{ exp.company_name }}</div>
                        <div class="exp-position">{{ exp.position }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 右侧简历详情 -->
            <div class="resume-detail" v-if="resumeDetail">
              <div class="cv-detail">
                <div v-if="resumeDetail.basic" class="detail-header">
                  <div class="avatar-large">
                    <img :src="resumeDetail.basic.head_url" alt="avatar" class="avatar-image" />
                  </div>
                  <div class="detail-basic">
                    <div class="name-age">
                      {{ resumeDetail.basic.user_name }}
                      <span style="font-size: 14px; color: #999999; margin-left: 10px"
                        >{{ resumeDetail.basic.age }}岁</span
                      >
                    </div>
                    <div class="intention-salary">
                      <span class="intention">求职意向：{{ selectedResume.expect_job }}</span>
                      <span class="salary">{{ selectedResume.expect_city }}</span>
                      <span class="salary"
                        >{{ resumeDetail.basic.expected_salary_min }}k -
                        {{ resumeDetail.basic.expected_salary_max }}k</span
                      >
                    </div>
                    <div class="contact-info">
                      <span class="phone">{{ resumeDetail.phone }}</span>
                      <span class="email">{{ resumeDetail.email }}</span>
                    </div>
                  </div>
                </div>

                <!-- 教育经历 -->
                <div v-if="resumeDetail.resume_education?.list?.length > 0" class="section">
                  <div class="section-title">教育经历</div>
                  <div class="section-content">
                    <div class="education-container">
                      <div
                        class="education-item"
                        v-for="(edu, index) in resumeDetail.resume_education?.list || []"
                        :key="index">
                        <div class="edu-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="edu-info">
                          <div class="edu-school">{{ edu.school_name }}</div>
                          <div class="edu-row">
                            <div class="edu-period">{{ edu.start_date }} — {{ edu.end_date }}</div>
                            <div class="edu-detail">{{ edu.degree }}·{{ edu.major }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 自我描述 -->
                <div v-if="resumeDetail.introduction" class="section">
                  <div class="section-title">自我描述</div>
                  <div class="section-content">
                    <div class="self-intro">{{ resumeDetail.introduction }}</div>
                  </div>
                </div>

                <!-- 工作经历 -->
                <div class="section">
                  <div class="section-title">工作经历</div>
                  <div class="section-content">
                    <div class="work-container">
                      <div
                        class="work-item"
                        v-for="(work, index) in resumeDetail?.resume_work_experience?.list || []"
                        :key="index">
                        <div class="work-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="work-info">
                          <div class="work-row">
                            <div class="work-company-info">
                              <div class="work-company">{{ work.company_name }}<span v-if="work.position">·{{ work.position }}</span></div>
                            </div>
                            <div class="work-period">{{ work.start_date }} — {{ work.end_date }}</div>
                          </div>
                          <div v-if="work.experience_description" class="work-description">
                            {{ work.experience_description }}
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 在校经历 -->
                <div class="section">
                  <div class="section-title">在校经历</div>
                  <div class="section-content">
                    <div class="school-container">
                      <div
                        class="school-item"
                        v-for="(school, index) in resumeDetail?.resume_school_experience?.list || []"
                        :key="index">
                        <div class="school-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="school-info">
                          <div class="school-row">
                            <div class="school-position">{{ school.position }}</div>
                            <div class="school-period">{{ school.start_date }} — {{ school.end_date }}</div>
                          </div>
                          <div class="school-description">{{ school.experience_description }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 技能信息 -->

                <div v-if="resumeDetail?.resume_skill?.list?.length" class="section">
                  <div class="section-title">技能信息</div>
                  <div class="section-content">
                    <div class="skill-container">
                      <div
                        class="skill-item"
                        v-for="(skill, index) in resumeDetail?.resume_skill?.list || []"
                        :key="index">
                        <div class="skill-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="skill-info">
                          <div class="skill-name">{{ skill.skill_name }}</div>
                          <div class="skill-level">{{ skill.skill_level }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 项目经历  -->
                <div class="section">
                  <div class="section-title">项目经历</div>
                  <div class="section-content">
                    <div class="project-container">
                      <div
                        class="project-item"
                        v-for="(project, index) in resumeDetail?.resume_project_experience?.list || []"
                        :key="index">
                        <div class="project-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="project-info">
                          <div class="project-row">
                            <div class="project-name">{{ project.project_name }}</div>
                            <div class="project-period">{{ project.start_date }} — {{ project.end_date }}</div>
                          </div>
                          <div class="project-description">{{ project.project_description }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <div v-if="activeTab !== 'unsuitable'" class="btn" @click="handleStatusChange(selectedResume, 'unsuitable')">不合适</div>
                <div v-if="activeTab !== 'interested'" class="btn btn-blue" @click="handleStatusChange(selectedResume, 'interested')">
                  有意向
                </div>
                <div v-if="activeTab === 'interested'" class="btn btn-blue" @click="handleStatusChange(selectedResume, 'proposed')">
                  拟录用
                </div>
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

  <!-- 标记不合适弹窗 -->
  <div v-if="showUnsuitableDialog" class="unsuitable-dialog">
    <div class="dialog-content">
      <div class="dialog-header">
        <h3>标记不合适</h3>
        <span class="close-btn" @click="handleUnsuitableCancel">&times;</span>
      </div>
      <div class="dialog-body">
        <p class="required">*请选择不合适原因并发送给求职者：</p>
        <div class="select-container">
          <select v-model="unsuitableReason" class="reason-select">
            <option value="">请选择不合适原因</option>
            <option v-for="reason in unsuitableReasons" :key="reason.value" :value="reason.value">
              {{ reason.label }}
            </option>
          </select>
        </div>
        <div class="message-container">
          <p class="message-title">尊敬的求职者：</p>
          <p class="message-content">
            非常感谢您对我司的{{ positionName }}感兴趣，您的简历已认真评阅，有诸多出彩的地方。
            然而由于岗位的一些特别要求，经过与用人部门讨论后仍然觉得匹配性稍显不足，
            因此，将不再做进一步推荐，希望您能理解。再次感谢您的投递，
            预祝求职顺利！
          </p>
        </div>
      </div>
      <div class="dialog-footer">
        <button class="cancel-btn" @click="handleUnsuitableCancel">取消</button>
        <button class="confirm-btn" @click="handleUnsuitableConfirm">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useResumeStore } from '../store/resume';
import { useRouter } from 'vue-router';
import { loginApi } from '@/services/api.js';
import { showToast } from 'vant';
import { getDevScreen } from '@/utils/index.js';

const resumeStore = useResumeStore();
const router = useRouter();
const positionCategories = ref([]);
const currentCategory = ref(null);
const studentResumes = ref([]);
const companyLoginInfo = ref(null);
const tabs = [
  { label: '待处理', value: 'pending' },
  { label: '有意向', value: 'interested' },
  { label: '不合适', value: 'unsuitable' },
  { label: '拟录用', value: 'proposed' },
];
const searchText = ref('');
const activeTab = ref('pending');
const selectedResume = ref(null);
const resumeDetail = ref(null);

const loading = ref(false);
const companyInfo = ref(null);

// 标记不合适弹窗相关
const showUnsuitableDialog = ref(false);
const unsuitableReason = ref('');
const unsuitableReasons = [
  { value: 'education', label: '学历不符合要求' },
  { value: 'experience', label: '工作经验不符合要求' },
  { value: 'skill', label: '技能不符合要求' },
  { value: 'other', label: '其他原因' }
];

// 职位名称
const positionName = ref('');

const switchCategory = async (category) => {
  currentCategory.value = category;
  resumeDetail.value = null
  await fetchJobApplyList(category);
};
// 获取职位申请简历列表
const fetchJobApplyList = async (category) => {
  loading.value = true;
  try {
    const response = await loginApi.getJobApplyList({
      device_id: resumeStore.deviceId,
      company_id: companyInfo.value.company_id,
      school_id: resumeStore.schoolId,
      publish_id: category.publish_id,
      type: activeTab.value,
      keywords: searchText.value,
    });
    if (response.code !== 1) {
      showToast(response.msg || '获取简历数据失败');
      return;
    }
    if (response.code === 1 && Array.isArray(response.data)) {
      // 处理包含code字段的响应
      studentResumes.value = response.data;
      console.log('获取到的简历列表:', studentResumes.value);
    }
  } catch (err) {
    console.error('获取申请列表失败:', err);
  } finally {
    loading.value = false;
  }
};

const switchTab = async (value) => {
  resumeDetail.value = null
  activeTab.value = value;
  if (currentCategory.value) {
    await fetchJobApplyList(currentCategory.value);
  }
};

const selectResume = async (resume) => {
  console.log(resume);
  selectedResume.value = resume;
  const res = await loginApi.getResumeDetail({
    apply_id: resume.apply_id,
  });
  if (res.code === 1) {
    resumeDetail.value = res.data;
    resumeDetail.value.introduction = resumeDetail.value.resume_attribute.list.find(item => item.attr_name === 'introduction')?.attribute_value || '';
    console.log('value:', resumeDetail.value);
  }
};

const handleStatusChange = async (resume, status) => {
  if (status === 'unsuitable') {
    // 显示标记不合适弹窗
    positionName.value = currentCategory.value?.job_name || '';
    showUnsuitableDialog.value = true;
  } else {
    // 直接更新其他状态
    const res = await loginApi.updateStatus({
      apply_ids: resume.apply_id,
      company_id: companyInfo.value.company_id,
      hr_id: companyLoginInfo.value.hr_id,
      status,
    });
    if (res.code === 1) {
      showToast('操作成功');
      await fetchJobApplyList(currentCategory.value);

    } else {
      showToast(res.msg || '操作失败');
    }
  }
};

// 处理标记不合适确认
const handleUnsuitableConfirm = async () => {
  if (!unsuitableReason.value) {
    showToast('请选择不合适原因');
    return;
  }
  
  const res = await loginApi.updateStatus({
    apply_ids: selectedResume.value.apply_id,
    company_id: companyInfo.value.company_id,
    hr_id: companyLoginInfo.value.hr_id,
    status: 'unsuitable',
    reason: unsuitableReason.value
  });
  
  if (res.code === 1) {
    showToast('操作成功');
    showUnsuitableDialog.value = false;
    unsuitableReason.value = '';
    await fetchJobApplyList(currentCategory.value);
  } else {
    showToast(res.msg || '操作失败');
  }
};

// 处理标记不合适取消
const handleUnsuitableCancel = () => {
  showUnsuitableDialog.value = false;
  unsuitableReason.value = '';
};
// 页面初始化时获取数据
const pageInit = async () => {
  if (!resumeStore.deviceId) {
    if (window.android && window.android.getDeviceId) {
      const deviceId = window.android.getDeviceId();
      if (deviceId) {
        resumeStore.setDeviceId(deviceId);
        console.log('初始化获取到设备ID:', deviceId);
      }
    }
  }
  companyLoginInfo.value = resumeStore.companyLoginInfo;
  const companyLoginInfoStr = localStorage.getItem('companyLoginInfo');
  if (!companyLoginInfo.value && companyLoginInfoStr) {
    companyLoginInfo.value = JSON.parse(companyLoginInfoStr);
  }
  if (companyLoginInfo.value) {
    const res = await loginApi.getCompanyDetail({
      device_id: resumeStore.deviceId,
      company_id: companyLoginInfo.value.company_id,
      school_id: resumeStore.schoolId,
      page: 1,
      page_size: 100,
    });
    if (res.code === 1) {
      companyInfo.value = res.data.company;
      await getJobApplyList();

      // 默认选择第一个职位并加载数据
      if (positionCategories.value.length > 0) {
        currentCategory.value = positionCategories.value[0];
        await fetchJobApplyList(positionCategories.value[0]);
      }
    }
    console.log(res);
  }
};

const handleSearch = () => {
  console.log('搜索文本:', searchText.value);
  if (searchText.value) {
    fetchJobApplyList(currentCategory.value);
  }
};
// 招聘中的职位
const getJobApplyList = async () => {
  const res = await loginApi.getJobApplyCount({
    device_id: resumeStore.deviceId,
    company_id: companyInfo.value.company_id,
    school_id: resumeStore.schoolId,
  });
  if (res.code === 1) {
    positionCategories.value = res.data;
  }
  console.log('获取职位分类:', res);
};

const logout = () => {
  // 发送消息给主屏
  try {
    if (window.android && window.android.notifyMainScreenUpdate) {
      window.android.notifyMainScreenUpdate(JSON.stringify({type: 'logout'}));
      console.log('已发送退出登录消息给主屏');
    } else {
      console.error('notifyMainScreenUpdate方法未定义');
    }
  } catch (error) {
    console.error('发送退出登录消息失败:', error);
  }
  
  // 执行退出登录操作
  resumeStore.logout();
  localStorage.setItem('companyLoginInfo', '');
  router.replace('/');
};

const updateData = async () => {
  const res = await loginApi.getCompanyDetail({
      device_id: resumeStore.deviceId,
      company_id: companyInfo.value.company_id,
      school_id: resumeStore.schoolId,
      page: 1,
      page_size: 100,
    });
    if (res.code === 1) {
      companyInfo.value = res.data.company;
      await getJobApplyList();
    }
};

// 处理屏幕更新通知
window.onScreenUpdate = function (event) {
  console.log('收到屏幕更新通知:', event);
  if (event === 'resume_delivered') {
    // 当收到简历投递成功的通知时，更新数据
    console.log('简历投递成功，更新数据');
    updateData()
  }
};

// 页面加载时获取数据
onMounted(async () => {
  getDevScreen();
  await pageInit();
  
  // 监听设备ID事件
  window.addEventListener('deviceIdReceived', (event) => {
    const id = event.detail;
    if (resumeStore) {
      resumeStore.setDeviceId(id);
    }
    console.log('CvList页面收到设备ID:', id);
  });
  
  // 监听卡片信息事件
  window.addEventListener('cardInfoReceived', (event) => {
    const cardInfo = event.detail;
    console.log('CvList页面收到卡片信息:', cardInfo);
  });
  
  // 监听状态信息事件
  window.addEventListener('statusMessage', (event) => {
    const { message, type } = event.detail;
    console.log('CvList页面收到状态信息:', message, type);
  });
});
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
  padding: 10px 20px;
  background-color: #fff;
  height: 60px;
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  .header-left {
    @include flex-fun(row, flex-start, center);
    flex: 1;
    .company-logo {
      .logo-image {
        width: auto;
        max-width: 150px;
        max-height: 60px;
      }
    }
  }

  .header-right {
    @include flex-fun(row, flex-start, center);
  }
}

.content {
  width: 100%;
  flex: 1;
}

.no-applicant {
  padding: 16px;
  text-align: center;
  color: #999999;
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

  .no-category {
    padding: 16px;
    text-align: center;
    color: #999999;
  }
}

.sidebar-header {
  font-size: 16px;
  font-weight: 600;
  color: #333333;
  padding: 16px;
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
  transition: background-color 0.3s;
  cursor: pointer;
}

.category-item.active {
  background-color: #f7fbff;
  border-radius: 30px 0 0 30px;
}

.category-count {
  font-size: 12px;
  color: #f74d4d;
  padding: 2px 8px;
  border-radius: 10px;
}

.right-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #e2f1ff;
  border-top-left-radius: 12px;
  background-color: #f7fbff;
  padding: 10px;
  margin-left: 10px;
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
  border-bottom: 1px solid #eeeeee;
}

.tab-item {
  padding: 6px 18px;
  font-size: 14px;
  color: #333333;
  background-color: #fafafa;
  border-bottom: 1px solid #e8ecf4;
  position: relative;
  border: 1px solid #eeeeee;
  margin-bottom: -2px;
}

.tab-item + .tab-item {
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
  width: 380px;
  flex-shrink: 0;
  background-color: #ffffff;
  padding-right: 14px;
  height: 100%;
  overflow-y: auto;
}

.applicant-item {
  font-size: 16px;
  padding: 12px;
  cursor: pointer;
  background-color: #f7fbff;
  border: 1px solid #e2f1ff;
  border-radius: 4px;
  position: relative;
  .applicant-header {
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-bottom: 12px;
  }
  &::after {
    content: '';
    position: absolute;
    right: -3px;
    top: 50%;
    transform: rotate(45deg) translateY(-50%);
    width: 10px;
    height: 10px;
    border: 1px solid #3ba0fe;
    border-left: none;
    border-bottom: none;
    display: none;
  }
  &.active::after {
    display: block;
  }
  &.active {
    border-color: #3ba0fe;
  }
  .avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    overflow: hidden;
    background-color: #0080ff;
    color: #ffffff;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 24px;
    font-weight: 600;
    margin-right: 12px;
    .avatar-image {
      width: 100%;
      height: 100%;
    }
  }
  .applicant-basic {
    flex: 1;
  }
  .name-info {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    margin-bottom: 4px;
  }

  .name {
    font-weight: 600;
    color: #333333;
  }

  .gender {
    font-size: 0px;
    .svg-icon {
      width: 14px;
      height: 14px;
      margin-left: 0;
    }
  }

  .age {
    color: #999999;
    font-size: 14px;
  }

  .apply-time {
    color: #999999;
    font-size: 14px;
    margin-left: auto;
  }

  .intention-salary {
    display: flex;
    flex-direction: row;
    align-items: center;
    gap: 12px;
    font-size: 12px;
    color: #555555;
  }

  .salary {
    padding-left: 5px;
    border-left: 1px solid #dadada;
    color: #555555;
  }

  .work-experience {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .exp-item {
    @include flex-fun(row, flex-start, center);
    font-size: 12px;
    color: #555555;

    .exp-bullet {
      margin-right: 8px;
    }
    .svg-icon {
      width: 14px;
      height: 15px;
      margin-left: 0;
    }
    .exp-content {
      flex: 1;
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      font-size: 14px;
    }
    .exp-period,
    .exp-info {
      color: #555555;
      margin-right: 10px;
    }
  }
}

.applicant-item + .applicant-item {
  margin-top: 10px;
}

.resume-detail {
  flex: 1;
  padding: 10px;
  height: 100%;
  box-sizing: border-box;
  background-color: #ffffff;
  border-radius: 4px;
  position: relative;
  padding-bottom: 0px;
  border: 1px solid #eeeeee;
  &.empty {
    display: flex;
    justify-content: center;
    align-items: center;
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
    overflow: hidden;
    img {
      width: 100%;
    }
  }

  .section {
    margin-bottom: 24px;
    .section-title {
      font-size: 16px;
      font-weight: 600;
      color: #333333;
      margin-bottom: 12px;
      height: 40px;
      padding-left: 20px;
      @include flex-fun(row, flex-start, center);
      background-color: #fafafa;
    }

    .section-content {
      padding-left: 20px;
      display: flex;
      flex-direction: column;
    }
  }
}

.cv-detail {
  height: 100%;
  overflow-y: auto;
  padding-bottom: 60px;
  box-sizing: border-box;
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
  font-weight: 500;
  padding-left: 5px;
  border-left: 1px solid #dadada;
  color: #555555;
}

.contact-info {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #999999;
}

.education-container {
  position: relative;
}

.education-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 14px;
  margin-bottom: 24px;
  position: relative;
}

.edu-bullet {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.bullet-circle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #0080ff;
  background-color: white;
  position: relative;
  z-index: 1;
  margin-top: 2px;
}

.bullet-line {
  position: absolute;
  top: 20px;
  left: 5px;
  width: 2px;
  height: calc(100% + 24px);
  background-color: #E2F1FF;
  z-index: 0;
}

.edu-info {
  flex: 1;
  color: #555555;
  display: flex;
  flex-direction: column;
}

.edu-school {
  font-weight: 500;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333333;
}

.edu-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 24px;
}

.edu-period {
  color: #555555;
  font-size: 13px;
  width: auto;
  flex-shrink: 0;
}

.edu-detail {
  font-size: 13px;
  color: #666666;
}

.self-intro {
  font-size: 14px;
  color: #555555;
  line-height: 1.5;
}

.work-container {
  position: relative;
}

.work-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 14px;
  margin-bottom: 24px;
  position: relative;
}

.work-bullet {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.work-info {
  flex: 1;
  color: #555555;
  display: flex;
  flex-direction: column;
}

.work-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.work-company-info {
  flex: 1;
}

.work-company {
  font-weight: 500;
  font-size: 14px;
  color: #333333;
}

.work-period {
  color: #999999;
  font-size: 13px;
  width: auto;
  flex-shrink: 0;
  margin-left: 24px;
}

.work-description {
  font-size: 13px;
  color: #666666;
  line-height: 1.4;
}

.school-container {
  position: relative;
}

.school-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 14px;
  margin-bottom: 24px;
  position: relative;
}

.school-bullet {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.school-info {
  flex: 1;
  color: #555555;
  display: flex;
  flex-direction: column;
}

.school-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.school-position {
  flex: 1;
  font-weight: 500;
  font-size: 14px;
  color: #333333;
}

.school-period {
  color: #999999;
  font-size: 13px;
  width: auto;
  flex-shrink: 0;
  margin-left: 24px;
}

.school-description {
  font-size: 13px;
  color: #666666;
  line-height: 1.4;
}

.project-container {
  position: relative;
}

.project-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 14px;
  margin-bottom: 24px;
  position: relative;
}

.project-bullet {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.project-info {
  flex: 1;
  color: #555555;
  display: flex;
  flex-direction: column;
}

.project-row {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.project-name {
  flex: 1;
  font-weight: 500;
  font-size: 14px;
  color: #333333;
}

.project-period {
  color: #999999;
  font-size: 13px;
  width: auto;
  flex-shrink: 0;
  margin-left: 24px;
}

.project-description {
  font-size: 13px;
  color: #666666;
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
  box-shadow: 1px 0px 6px rgba(0, 0, 0, 0.05);
  z-index: 10;
}

.btn {
  background-color: #ffffff;
  color: #555555;
  border: 1px solid #eeeeee;
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
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
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

/* 标记不合适弹窗样式 */
.unsuitable-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.dialog-content {
  background-color: #ffffff;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333333;
}

.close-btn {
  font-size: 20px;
  color: #999999;
  cursor: pointer;
  transition: color 0.3s;
}

.close-btn:hover {
  color: #666666;
}

.dialog-body {
  padding: 20px;
}

.required {
  color: #ff4d4f;
  font-size: 14px;
  margin-bottom: 16px;
}

.select-container {
  margin-bottom: 24px;
}

.reason-select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  color: #333333;
  outline: none;
  transition: all 0.3s;
}

.reason-select:focus {
  border-color: #0080ff;
  box-shadow: 0 0 0 2px rgba(0, 128, 255, 0.2);
}

.message-container {
  margin-bottom: 24px;
}

.message-title {
  font-size: 14px;
  font-weight: 500;
  color: #333333;
  margin-bottom: 8px;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
  color: #666666;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
  gap: 12px;
}

.cancel-btn {
  padding: 8px 16px;
  font-size: 14px;
  color: #666666;
  background-color: #ffffff;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  border-color: #0080ff;
  color: #0080ff;
}

.confirm-btn {
  padding: 8px 16px;
  font-size: 14px;
  color: #ffffff;
  background-color: #0080ff;
  border: 1px solid #0080ff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.confirm-btn:hover {
  background-color: #0066cc;
  border-color: #0066cc;
}
</style>
