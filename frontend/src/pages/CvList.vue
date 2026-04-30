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
          <van-search
            v-model="searchText"
            placeholder="搜索学生"
            shape="round"
            @search="handleSearch" />
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
                    <div class="exp-item" v-for="(exp, index) in resume.last_work" :key="index">
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
                      <span class="name">{{ resumeDetail.basic.user_name }}</span>
                      <span class="age">{{ resumeDetail.basic.age }}岁</span>
                      <div class="contact-info">
                        <div class="phone">
                          <img src="@/assets/svgs/phone.svg" alt="phone" class="svg-icon" />
                          <span>{{ resumeDetail.basic.mobile }}</span>
                        </div>
                        <div class="email">
                          <img src="@/assets/svgs/email.svg" alt="email" class="svg-icon" />
                          <span>{{ resumeDetail.basic.email }}</span>
                        </div>
                      </div>
                    </div>
                    <div class="intention-salary">
                      <span class="intention">求职意向：{{ selectedResume.expect_job }}</span>
                      <span class="salary">{{ selectedResume.expect_city }}</span>
                      <span class="salary"
                        >{{ resumeDetail.basic.expected_salary_min }}k -
                        {{ resumeDetail.basic.expected_salary_max }}k</span
                      >
                    </div>
                  </div>
                </div>

                <!-- 教育经历 -->
                <div v-if="resumeDetail.resume_education?.list?.length > 0" class="section">
                  <div class="section-title">教育经历</div>
                  <div class="section-content">
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(edu, index) in resumeDetail.resume_education?.list || []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-school">{{ edu.school_name }}</div>
                          <div class="tem-row">
                            <div class="tem-period" style="color: #555555">
                              {{ edu.start_date }} — {{ edu.end_date }}
                            </div>
                            <div class="tem-detail">{{ edu.degree }}·{{ edu.major }}</div>
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
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(work, index) in resumeDetail?.resume_work_experience?.list || []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-row sub-row">
                            <div class="tem-company-info">
                              <div class="tem-company">
                                {{ work.company_name
                                }}<span v-if="work.position">·{{ work.position }}</span>
                              </div>
                            </div>
                            <div class="tem-period">
                              {{ work.start_date }} — {{ work.end_date }}
                            </div>
                          </div>
                          <div v-if="work.experience_description" class="tem-description">
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
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(school, index) in resumeDetail?.resume_school_experience?.list ||
                        []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-row sub-row">
                            <div class="tem-position">{{ school.position }}</div>
                            <div class="tem-period">
                              {{ school.start_date }} — {{ school.end_date }}
                            </div>
                          </div>
                          <div class="tem-description">{{ school.experience_description }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 培训信息 -->
                <div v-if="resumeDetail?.resume_training_experience?.list?.length" class="section">
                  <div class="section-title">培训信息</div>
                  <div class="section-content">
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(training, index) in resumeDetail?.resume_training_experience
                          ?.list || []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-row sub-row">
                            <div class="tem-position">{{ training.training_institution }}</div>
                            <div class="tem-period">
                              {{ training.start_date }} — {{ training.end_date }}
                            </div>
                          </div>
                          <div class="tem-description">{{ training.experience_description }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 技能信息 -->
                <div v-if="resumeDetail?.resume_skill?.list?.length" class="section">
                  <div class="section-title">技能信息</div>
                  <div class="section-content">
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(skill, index) in resumeDetail?.resume_skill?.list || []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-row">
                            <div class="tem-position">{{ skill.skill_name }}</div>
                            <div class="tem-period">{{ skill.skill_level }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 技能描述 -->
                <div v-if="resumeDetail?.skillArray?.length" class="section">
                  <div class="section-title">技能描述</div>
                  <div class="section-content">
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(skill, index) in resumeDetail?.skillArray || []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-row">
                            <div class="tem-position">{{ skill }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 校内荣誉 -->
                <div v-if="resumeDetail?.resume_honor?.list?.length" class="section">
                  <div class="section-title">校内荣誉</div>
                  <div class="section-content">
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(honor, index) in resumeDetail?.resume_honor?.list || []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-row sub-row">
                            <div class="tem-position">{{ honor.honor_name }}</div>
                            <div class="tem-period">{{ honor.honor_date }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <!-- 获得证书 -->
                <div v-if="resumeDetail?.resume_certificate?.list?.length" class="section">
                  <div class="section-title">已获证书</div>
                  <div class="section-content">
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(certificate, index) in resumeDetail?.resume_certificate?.list || []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-row sub-row">
                            <div class="tem-position">{{ certificate.certificate_name }}</div>
                            <div class="tem-period">{{ certificate.certificate_date }}</div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 项目经历  -->
                <div class="section">
                  <div class="section-title">项目经历</div>
                  <div class="section-content">
                    <div class="tem-container">
                      <div
                        class="tem-item"
                        v-for="(project, index) in resumeDetail?.resume_project_experience?.list ||
                        []"
                        :key="index">
                        <div class="tem-bullet">
                          <div class="bullet-circle"></div>
                          <div class="bullet-line"></div>
                        </div>
                        <div class="tem-info">
                          <div class="tem-row sub-row">
                            <div class="tem-position">{{ project.project_name }}</div>
                            <div class="tem-period">
                              {{ project.start_date }} — {{ project.end_date }}
                            </div>
                          </div>
                          <div class="tem-description">{{ project.project_description }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="action-buttons">
                <div
                  v-if="unsuitableBtn"
                  class="btn"
                  @click="handleStatusChange(selectedResume, 'unsuitable')">
                  不合适
                </div>
                <div
                  v-if="interestedBtn"
                  class="btn btn-blue"
                  @click="handleStatusChange(selectedResume, 'interested')">
                  有意向
                </div>
                <div
                  v-if="proposedBtn"
                  class="btn btn-blue"
                  @click="handleStatusChange(selectedResume, 'proposed')">
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
            因此，将不再做进一步推荐，希望您能理解。再次感谢您的投递， 预祝求职顺利！
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
import { onMounted, onUnmounted, ref, computed } from 'vue';
import { useResumeStore } from '../store/resume';
import { useRouter } from 'vue-router';
import { loginApi } from '@/services/api.js';
import { showToast } from 'vant';
import { getDevScreen } from '@/utils/index.js';
import { sendToMainScreen } from '../utils/androidBridge.js';

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
  { value: '学历不符合要求', label: '学历不符合要求' },
  { value: '工作经验不符合要求', label: '工作经验不符合要求' },
  { value: '技能不符合要求', label: '技能不符合要求' },
  { value: '其他原因', label: '其他原因' },
];

// 按钮显示判断
const unsuitableBtn = computed(() => {
  return activeTab.value !== 'unsuitable';
});
const interestedBtn = computed(() => {
  if (activeTab.value === 'interested') {
    return false;
  } else if (activeTab.value === 'proposed') {
    return false;
  }
  return true;
});
// 拟录用按钮显示判断
const proposedBtn = computed(() => {
  if (activeTab.value === 'proposed') {
    return false;
  } else if (activeTab.value === 'unsuitable') {
    return false;
  }
  return true;
});

// 职位名称
const positionName = ref('');

const switchCategory = async (category) => {
  selectedResume.value = null;
  currentCategory.value = category;
  resumeDetail.value = null;
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
  selectedResume.value = null;
  resumeDetail.value = null;
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
    resumeDetail.value.skillArray = [];
    const skillStr = resumeDetail.value.resume_attribute.list.find(
      (item) => item.attr_name === 'skill_desc',
    );
    if (skillStr) {
      let result = skillStr.attr_value.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
      console.log(result);
      resumeDetail.value.skillArray = result.split('\n');
    }
    resumeDetail.value.introduction =
      resumeDetail.value.resume_attribute.list.find((item) => item.attr_name === 'introduction')
        ?.attr_value || '';
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
  const formData = {
    apply_ids: selectedResume.value.apply_id,
    company_id: companyInfo.value.company_id,
    hr_id: companyLoginInfo.value.hr_id,
    status: 'unsuitable',
    reason: unsuitableReason.value,
  };
  console.log('标记不合适请求参数:', formData);
  const res = await loginApi.updateStatus(formData);

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
    if (window.android && window.android.hideKeyboard) {
      try {
        window.android.hideKeyboard();
      } catch (error) {
        console.error('调用hideKeyboard失败:', error);
      }
    }
    if (currentCategory.value && currentCategory.value.publish_id) {
      fetchJobApplyList(currentCategory.value);
    }
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
  sendToMainScreen('logout');
  
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

// 处理屏幕事件
const handleScreenEvent = (event) => {
  const { action, data } = event.detail;
  console.log('CvList页面收到屏幕事件:', action, data);

  if (action === 'resumeDelivered') {
    console.log('简历投递成功，更新数据');
    updateData();
  }
};

// 处理设备ID事件
const handleDeviceIdReceived = (event) => {
  const id = event.detail;
  resumeStore.setDeviceId(id);
  console.log('CvList页面收到设备ID:', id);
};

// 处理卡片信息事件
const handleCardInfoReceived = (event) => {
  const cardInfo = event.detail;
  console.log('CvList页面收到卡片信息:', cardInfo);
};

// 处理状态消息事件
const handleStatusMessage = (event) => {
  const { message, type } = event.detail;
  console.log('CvList页面收到状态信息:', message, type);
};

// 页面加载时获取数据
onMounted(async () => {
  getDevScreen();
  await pageInit();

  window.addEventListener('deviceIdReceived', handleDeviceIdReceived);
  window.addEventListener('cardInfoReceived', handleCardInfoReceived);
  window.addEventListener('statusMessage', handleStatusMessage);
  window.addEventListener('screenEvent', handleScreenEvent);
});

onUnmounted(() => {
  window.removeEventListener('deviceIdReceived', handleDeviceIdReceived);
  window.removeEventListener('cardInfoReceived', handleCardInfoReceived);
  window.removeEventListener('statusMessage', handleStatusMessage);
  window.removeEventListener('screenEvent', handleScreenEvent);
});
</script>

<style lang="scss" scoped>
@import '../assets/mixins.scss';
.page-container {
  background-color: #fff;
  @include flex-fun(column, flex-start, flex-start);
  min-height: 100vh;
  @include font-size(base);
}

.headers {
  width: 100%;
  @include flex-fun(row, flex-start, center);
  padding: 10px 20px;
  background-color: #fff;
  height: 60px;
  flex-shrink: 0;
  .header-left {
    @include flex-fun(row, flex-start, center);
    flex: 1;
    height: 100%;
    .company-logo {
      width: 60px;
      height: 60px;
      padding-top: 5px;
      overflow: hidden;
      .logo-image {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }
  }

  .header-right {
    @include flex-fun(row, flex-start, center);
  }

  &::v-deep(.van-search__content) {
    background-color: #e9e9e9;
  }
  &::v-deep(.van-search__content) input {
    @include font-size(base);
  }
  &::v-deep(.van-search__content) input::placeholder {
    color: #999999;
  }
}

.content {
  @include flex-fun(row, flex-start);
  overflow: hidden;
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
  @include font-size(xl);
  position: relative;
  padding-left: 48px;
  font-weight: 700;
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

.left-sidebar {
  width: 280px;
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
  @include font-size(lg);
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
  @include font-size(sm);
  color: #555555;
  transition: background-color 0.3s;
  cursor: pointer;

  .category-name {
    @include font-size(lg);
  }
}

.category-item.active {
  background-color: #d9e7f5;
  border-radius: 30px 0 0 30px;
}

.category-count {
  @include font-size(sm);
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
  background-color: #d6e8f9;
  padding: 10px;
}

.cv-list-container {
  background-color: #ffffff;
  padding: 10px;
  height: 100%;
  @include flex-fun(column, flex-start);
}

.tab-bar {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  width: 100%;
  padding: 0;
  background-color: #ffffff;
  border-bottom: 1px solid #eeeeee;
  .tab-item {
    padding: 6px 18px;
    @include font-size(base);
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
}

.cv-content {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  overflow: hidden;
  background-color: #ffffff;
  margin-top: 10px;
  height: calc(100vh - 150px);
}

.applicant-list {
  width: 420px;
  flex-shrink: 0;
  background-color: #ffffff;
  padding-right: 14px;
  height: 100%;
  overflow-y: auto;
}

.applicant-item {
  @include font-size(lg);
  padding: 12px;
  cursor: pointer;
  background-color: #d6e8f9;
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
    @include font-size(xxl);
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
    @include font-size(lg);
    margin-bottom: 4px;
  }

  .name {
    font-weight: 600;
    color: #333333;
  }

  .gender {
    font-size: 0px;
    margin: 0 10px;
    .svg-icon {
      width: 14px;
      height: 14px;
      margin-left: 0;
    }
  }

  .age {
    color: #999999;
    @include font-size(base);
  }

  .apply-time {
    color: #999999;
    @include font-size(base);
    margin-left: auto;
  }

  .work-experience {
    display: flex;
    flex-direction: column;
  }

  .exp-item {
    @include flex-fun(row, flex-start, center);
    @include font-size(sm);
    color: #555555;
    position: relative;

    & + .exp-item {
      margin-top: 10px;
    }
    & + .exp-item::before {
      content: '';
      position: absolute;
      left: 8px;
      top: -10px;
      background-color: #fff;
      width: 2px;
      height: 10px;
    }
    .exp-bullet {
      margin-right: 8px;
      .svg-icon {
        width: 16px;
        height: 16px;
        margin-left: 0;
      }
    }
    .exp-content {
      flex: 1;
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      @include font-size(base);
      align-items: center;
    }
    .exp-period,
    .exp-info {
      color: #555555;
      margin-right: 10px;
    }
    .exp-info,
    .exp-position {
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
  }

  .intention-salary {
    @include flex-fun(row, flex-start, center);
    @include font-size(sm);
    color: #555555;
    gap: 0;

    .salary {
      padding-left: 5px;
      border-left: 1px solid #dadada;
      color: #555555;
      margin-left: 6px;
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
    @include font-size(xxxl);
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
      @include font-size(lg);
      font-weight: 600;
      color: #333333;
      margin-bottom: 12px;
      height: 40px;
      padding-left: 20px;
      @include flex-fun(row, flex-start, center);
      background-color: #ebe9e9;
    }

    .section-content {
      padding-left: 20px;
      display: flex;
      flex-direction: column;
    }
  }
  .cv-detail {
    height: 100%;
    overflow-y: auto;
    padding-bottom: 60px;
    box-sizing: border-box;
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
    @include flex-fun(row, flex-start, center);
    @include font-size(lg);
    font-weight: 600;
    color: #333333;
    margin-bottom: 8px;
    .age {
      @include font-size(base);
      color: #999999;
      margin-left: 10px;
    }
    .contact-info {
      @include flex-fun(row, flex-end, center);
      @include font-size(sm);
      color: #999999;
      flex: 1;
      padding-right: 10px;
    }
    .phone,
    .email {
      @include flex-fun(row, flex-end, center);
      @include font-size(sm);
      .svg-icon {
        margin-right: 5px;
        width: 16px;
        height: 16px;
      }
    }
  }
  .empty-text {
    @include font-size(base);
    color: #999999;
  }
  .intention-salary {
    @include flex-fun(row, flex-start, center);
    @include font-size(base);
    margin-bottom: 8px;
  }
  .salary {
    font-weight: 500;
    padding-left: 5px;
    border-left: 1px solid #dadada;
    color: #555555;
    margin-left: 6px;
  }
}

.intention {
  color: #555555;
}

.tem-container {
  position: relative;
  .tem-item {
    @include flex-fun(row, flex-start, flex-start);
    @include font-size(base);
    padding-bottom: 24px;
    position: relative;
  }
  .tem-bullet {
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
    height: 60%;
    background-color: #e2f1ff;
    z-index: 0;
  }
  .tem-info {
    flex: 1;
    color: #555555;
    display: flex;
    flex-direction: column;
  }
  .tem-position {
  }
  .tem-school {
    font-weight: 500;
    margin-bottom: 8px;
    @include font-size(base);
    color: #333333;
  }

  .tem-row {
    @include flex-fun(row, flex-start, center);
    &.sub-row {
      justify-content: space-between;
    }
  }

  .tem-description {
    @include font-size(sm);
    color: #666666;
    line-height: 1.4;
    margin-top: 10px;
  }

  .tem-period {
    color: #999999;
    @include font-size(sm);
    width: auto;
    flex-shrink: 0;
  }

  .tem-detail {
    @include font-size(sm);
    color: #666666;
    margin-left: 10px;
  }
}

.self-intro {
  @include font-size(base);
  color: #555555;
  line-height: 1.5;
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
  @include font-size(base);
  margin-right: 10px;
}

.btn-blue {
  background-color: #0080ff;
  color: #ffffff;
  padding: 8px 24px;
  border-radius: 4px;
  @include font-size(base);
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
  @include font-size(base);
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
  @include font-size(base);
  color: #ff4d4f;
  margin-bottom: 16px;
}

.retry-btn {
  background-color: #0080ff;
  color: #ffffff;
  padding: 8px 24px;
  border-radius: 4px;
  @include font-size(base);
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
  @include font-size(lg);
  font-weight: 600;
  color: #333333;
}

.close-btn {
  @include font-size(xxxxl);
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
  @include font-size(base);
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
  @include font-size(base);
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
  @include font-size(base);
  font-weight: 500;
  color: #333333;
  margin-bottom: 8px;
}

.message-content {
  @include font-size(base);
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
  @include font-size(base);
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
  @include font-size(base);
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
