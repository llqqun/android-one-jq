import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { Popup, CheckboxGroup, Checkbox, Field, Button, Toast } from 'vant';
import App from './App.vue'
import router from './router'
import 'vant/lib/index.css'
import vconsole from 'vconsole'

window.vconsole = new vconsole()

// 重写localStorage的setItem方法，实现两个屏幕的数据同步
const originalSetItem = localStorage.setItem;
localStorage.setItem = function(key, value) {
    originalSetItem.call(this, key, value);
    // 调用Android原生方法同步数据
    if (window.android && window.android.syncLocalStorage) {
        window.android.syncLocalStorage(key, value);
    }
};

// 添加setLocalStorage方法，用于接收来自Android的数据同步
window.setLocalStorage = function(key, value) {
    localStorage.setItem(key, value);
};

const app = createApp(App)
app.use(Popup);
app.use(Field);
app.use(Button);
app.use(CheckboxGroup);
app.use(Checkbox);
app.use(Toast);
app.use(createPinia())
app.use(router)
app.mount('#app')