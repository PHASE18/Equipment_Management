// 应用启动入口：创建 Vue 实例，注册状态、路由和权限指令后挂载根组件。
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { setupPermissionDirective } from './directives/permission'
import './styles/main.css'

const app = createApp(App)

app.use(createPinia()) // 注册后，任何组件都可以通过 useStore() 之类的方式访问全局状态如购物车
app.use(router)
app.use(ElementPlus) // 注册 Element Plus（基于 Vue 3 的一套 UI 组件库）
setupPermissionDirective(app) // 注册后，根据用户权限，控制某些按钮/元素是否显示（常见于后台管理系统的按钮级权限控制）

app.mount('#app') // 将应用挂载到真实 DOM 上，这是启动应用的最后一步
