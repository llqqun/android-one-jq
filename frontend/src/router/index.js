import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('../pages/Home.vue')
  },
  {
    path: '/cvList',
    name: 'cvList',
    component: () => import('../pages/CvList.vue')
  },
  {
    path: '/resumeSubmission',
    name: 'resumeSubmission',
    component: () => import('../pages/ResumeSubmission.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router