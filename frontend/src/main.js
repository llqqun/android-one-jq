import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { Popup, CheckboxGroup, Checkbox } from 'vant';
import App from './App.vue'
import router from './router'
import 'vant/lib/index.css'
import vconsole from 'vconsole'
new vconsole()



const app = createApp(App)
app.use(Popup);
app.use(CheckboxGroup);
app.use(Checkbox);
app.use(createPinia())
app.use(router)
app.mount('#app')