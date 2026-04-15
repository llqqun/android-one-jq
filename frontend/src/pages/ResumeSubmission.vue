<template>
  <div class="page-container">
    <div class="headers">
      <div class="company-logo">公司logo</div>
      <div class="company-name">长沙市云聘教育科技有限公司</div>
    </div>

    <div class="content">
      <!-- 公司基本信息 -->
      <div class="col left-col">
        <!-- 第一个卡片：公司信息介绍 -->
        <div class="card first-card">
          <div class="info-header">
            <div class="info-logo">LOGO</div>
            <span class="info-title">长沙市云聘教育科技有限公司</span>
          </div>

          <div class="company-details">
            <div class="detail-item"><span class="label">单位行业：</span><span>教育</span></div>
            <div class="detail-item"><span class="label">单位性质：</span><span>教育单位</span></div>
            <div class="detail-item"><span class="label">单位规模：</span><span>150-500人</span></div>
            <div class="detail-item">
              <span class="label">单位地址：</span>
              <span>湖南省长沙市芙蓉区定王台街道东泰街附023号</span>
            </div>
          </div>

          <div class="circle-wrap">
            <CircleProgress percentage="66" type="arc">
              <div class="circle-box">
                <div class="circle-content">
                  <div class="rating-img">
                    <img src="../assets/svgs/qx.svg" alt="qr" class="svg" />
                    <div class="rating-value">BAA</div>
                  </div>
                  <span class="rating-text">云研企信等级</span>
                  <span class="rating-desc">超越 <span class="highlight">88.35%</span> 的校园招聘企业</span>
                </div>
              </div>
            </CircleProgress>
          </div>
        </div>

        <!-- 第二个卡片：二维码 -->
        <div class="card second-card">
          <div class="qr-placeholder">
            <div class="qr-box">
              <div class="corner-left-top"></div>
              <div class="corner-right-top"></div>
              <div class="corner-left-bottom"></div>
              <div class="corner-right-bottom"></div>
              <img src="../assets/images/test-code.png" class="img" alt="二维码" />
            </div>
          </div>
          <!-- 下方提示 -->
          <div class="tip-section">
            <div class="tip-img">
              <img src="../assets/images/sm.png" class="img" alt="sm" />
            </div>
            <span class="tip-text">学生，请扫码</span>
            <span class="tip-text">HR屏可弹出电子简历</span>
          </div>
        </div>
      </div>
      <!-- 公司简介和招聘简章 -->
      <div class="col middle-col">
        <div class="section-title">单位简介</div>
        <div class="section-box">
          <span>WEGYMER健圈”成立于2016年，是中国“大健康、大体育”产业细分领域的先行企业，公司核心...</span>
        </div>
        <div class="section-title">招聘简章</div>
        <div class="section-box">
          <span>公司始终以“正道成功”的理念，致力于开发中国体育人才未来，帮助更多中国人养成受益一生的运动习惯。</span>
          <button @click="openDialog(1)">测试弹窗按钮1</button>
          <button @click="openDialog(2)">测试弹窗按钮2</button>
          <button @click="openDialog(3)">测试弹窗按钮3</button>
        </div>
      </div>
      <!-- 招聘岗位 -->
      <div class="col right-col">
        <div class="section-title">招聘职位</div>
        <div class="section-list-box">
          <div class="job-item" v-for="(job, idx) in jobs" :key="idx" @click="handleJobClick(job)">
            <div class="job-title">
              <div class="text-overflow">{{ job.title }}</div>
              <div class="job-salary">{{ job.salary }}</div>
            </div>
            <div class="job-info">
              <div>
                <div> 招聘人数：{{ job.count }} | {{ job.degree }} | {{ job.city }} </div>
                <div> 招聘专业：英语、商务英语 </div>
              </div>
              <div class="apply-btn">投递简历</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useResumeStore } from '../store/resume'
import CircleProgress from '../components/CircleProgress.vue'

const resumeStore = useResumeStore()
const jobs = resumeStore.jobs
const cvList = resumeStore.cvList

const openDialog = (type) => {
  console.log('打开弹窗类型：', type)
  // 这里可以使用Vant的Dialog组件
  if (type === 1) {
    console.log('请选择你需要投递的职位')
  } else if (type === 2) {
    console.log('选择简历')
  } else if (type === 3) {
    console.log('投递简历提示')
  }
}

const handleJobClick = (job) => {
  console.log('点击职位：', job)
  openDialog(2)
}

const handleCVDelivery = (cv) => {
  console.log('投递的简历：', cv)
  openDialog(3)
}

const handleExitLogin = () => {
  console.log('退出登录')
}

const handleContinueDelivery = () => {
  console.log('继续投递简历')
}
</script>

<style lang="scss" scoped>
@import '../assets/mixins.scss';

.page-container {
  @include flex-fun(column, flex-start, flex-start);
}

.headers {
  width: 100%;
  @include flex-fun(row, flex-start, center);
  padding: 0 20px;
  background-color: #fff;
  height: 50px;
  flex-shrink: 0;
}

.company-logo {}

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
  width: 100%;
  flex: 1;
  box-sizing: border-box;
  @include flex-fun(row, flex-start, stretch);
  padding: 12px 15px;
}

.col {
  background-color: #fff;
  border: 1px solid #e8ecf4;
  border-radius: 20px;
  box-sizing: border-box;
  min-height: 600px;
}

.left-col {
  background: none;
  border: none;
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-right: 10px;
}

.card {
  background-color: #fff;
  border: 1px solid #e8ecf4;
  border-radius: 10px;
  padding: 20px;
  box-sizing: border-box;
}

.first-card {
  padding: 14px 20px;
}

.circle-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 4px;
  margin-bottom: -50px;
}

.second-card {
  flex-grow: 1;
  padding: 10px 20px;
}

.circle-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-top: -12px;
}

.rating-img {
  width: 100%;
  height: 40px;
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.svg {
  width: 40px;
  height: 100%;
}

.rating-value {
  font-size: 32px;
  font-weight: 600;
  color: #e98f36;
}

.rating-text {
  font-size: 12px;
  color: #555555;
}

.rating-desc {
  font-size: 10px;
  color: #555555;
  text-align: center;
}

.highlight {
  color: #e98f36;
}

.info-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px dashed #eeeeee;
}

.info-logo {
  width: 48px;
  height: 48px;
  border: 1px solid #79afff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #3f8bff;
  font-size: 14px;
}

.info-title {
  font-size: 16px;
  font-weight: 700;
  color: #161c2b;
  text-align: center;
  line-height: 1.3;
}

.company-details {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-item {
  display: flex;
  flex-direction: row;
  align-items: flex-start;
  font-size: 13px;
  color: #555555;
  line-height: 1.4;
  word-wrap: break-word;
}

.label {
  margin-right: 4px;
  white-space: nowrap;
}

.qr-placeholder {
  display: flex;
  justify-content: center;
  background-color: #e2f1ff;
  border-radius: 6px;
  width: 140px;
  height: 140px;
  margin: 0 auto;
  align-items: center;
}

.qr-box {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #a8b4c7;
  font-size: 12px;
  background-color: #fff;
  overflow: hidden;
}

.img {
  width: 100px;
  height: 100px;
}

.qr-box .corner-left-top,
.qr-box .corner-right-top,
.qr-box .corner-left-bottom,
.qr-box .corner-right-bottom {
  position: absolute;
  border: 1px solid #0180ff;
  border-width: 0;
  width: 10px;
  height: 10px;
}

.qr-box .corner-left-top {
  left: 0;
  top: 0;
  border-top-width: 2px;
  border-left-width: 2px;
}

.qr-box .corner-right-top {
  right: 0;
  top: 0;
  border-top-width: 2px;
  border-right-width: 2px;
}

.qr-box .corner-left-bottom {
  left: 0;
  bottom: 0;
  border-bottom-width: 2px;
  border-left-width: 2px;
}

.qr-box .corner-right-bottom {
  right: 0;
  bottom: 0;
  border-bottom-width: 2px;
  border-right-width: 2px;
}

.tip-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-top: 10px;
}

.tip-img {
  width: 40px;
  height: 40px;
  background-color: #0080ff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #a8b4c7;
  font-size: 0;
}

.tip-img .img {
  width: 24px;
  height: 24px;
}

.tip-text {
  font-size: 12px;
  color: #666;
}

.middle-col {
  width: 38%;
  padding: 12px;
  margin-right: 10px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
  background-color: #f7fbff;
  height: 28px;
  line-height: 28px;
  padding: 0 26px;
  position: relative;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 18px;
  top: 50%;
  transform: translateY(-50%);
  background-color: #0080ff;
  width: 2px;
  height: 11px;
}

.section-box {
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
  color: #4a5768;
  line-height: 1.6;
  white-space: pre-wrap;
}

.right-col {
  width: 38%;
  padding: 12px;
}

.section-list-box {
  height: 610px;
  overflow-y: auto;
}

.job-item {
  border: 1px solid #e2f1ff;
  border-radius: 8px;
  padding: 14px;
  margin-top: 16px;
  background-color: #f7fbff;
}

.job-title {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  min-width: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333333;
  margin-bottom: 6px;
}

.text-overflow {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding-right: 6px;
  display: inline-block;
}

.job-salary {
  flex-shrink: 0;
  font-size: 12px;
  color: #1171ff;
  font-weight: 600;
}

.job-info {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #555555;
}

.apply-btn {
  background-color: #0080ff;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
}
</style>