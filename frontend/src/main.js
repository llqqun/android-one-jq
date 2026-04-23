import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { Popup, CheckboxGroup, Checkbox, Field, Button, Toast, Icon, Search } from 'vant';
import App from './App.vue'
import router from './router'
import 'vant/lib/index.css'
import vconsole from 'vconsole'

window.vconsole = new vconsole()

const app = createApp(App)
app.use(Popup);
app.use(Field);
app.use(Icon);
app.use(Button);
app.use(CheckboxGroup);
app.use(Checkbox);
app.use(Toast);
app.use(createPinia())
app.use(router)
app.use(Search)
app.mount('#app')