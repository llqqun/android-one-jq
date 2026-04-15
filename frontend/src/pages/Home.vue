<template>
  <div class="home-container">
    <div class="img-box" @click="handleRoute">
      <img src="../assets/images/home_2.png" class="home-image-logo" alt="logo">
      <img src="../assets/images/home_3.png" class="home-image-text" alt="text">
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const handleRoute = () => {
  // 调用 Android 原生方法获取屏幕类型
  if (window.android && window.android.getScreenType) {
    const screenType = window.android.getScreenType()
    console.log('Screen type:', screenType)
    
    // 根据屏幕类型跳转到不同页面
    if (screenType === 'main') {
      // 主屏跳转到 resumeSubmission
      router.push('/resumeSubmission')
    } else if (screenType === 'secondary') {
      // 副屏跳转到 cvList
      router.push('/cvList')
    } else {
      // 默认跳转到 cvList
      router.push('/cvList')
    }
  } else {
    // 如果没有 Android 原生方法，默认跳转到 cvList
    router.push('/resumeSubmission')
  }
}
</script>

<style lang="scss" scoped>
@import '../assets/mixins.scss';

.home-container {
  height: 100%;
  min-height: 100vh;
  width: 100%;
  background-color: #F8F8F8;
  @include flex-fun(column, flex-start, flex-start);
  background-image: url('../assets/images/home_1.png');
  background-size: 100% 100%;
  background-repeat: no-repeat;
  color: #fff;
  overflow: hidden;
}

.img-box {
  position: relative;
  width: 100%;
  height: 100vh;
  cursor: pointer;
}

.home-image-logo {
  position: absolute;
  width: 240px;
  left: 40px;
  top: 40px;
}

.home-image-text {
  position: absolute;
  width: 600px;
  left: 40px;
  top: 80px;
}
</style>