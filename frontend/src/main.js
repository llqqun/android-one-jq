import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { Popup, CheckboxGroup, Checkbox, Field, Button, Toast, Icon, Search } from 'vant'
import App from './App.vue'
import router from './router'
import 'vant/lib/index.css'
import vconsole from 'vconsole'
import { initAndroidBridge } from './utils/androidBridge'

window.vconsole = new vconsole()

initAndroidBridge()

// 测试代码
// window.android = {
//     getDeviceId: () => '05132017062000003180902854042382',
//   getScreenType: () => 'main',
//   hideKeyboard: () => console.log('hideKeyboard called')
// }
function initApp() {
  try {
    const app = createApp(App)
    app.use(Popup)
    app.use(Field)
    app.use(Icon)
    app.use(Button)
    app.use(CheckboxGroup)
    app.use(Checkbox)
    app.use(Toast)
    app.use(createPinia())
    app.use(router)
    app.use(Search)
    
    router.isReady().then(() => {
      app.mount('#app')
      console.log('Vue app mounted successfully')
    }).catch(err => {
      console.error('Router ready failed:', err)
      app.mount('#app')
    })
  } catch (err) {
    console.error('App initialization failed:', err)
  }
}

if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initApp)
} else {
  initApp()
}