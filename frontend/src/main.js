import Vue from 'vue'
import App from './App.vue'

// 引入 Element UI 及其 CSS
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import router from './router'

Vue.config.productionTip = false

// 全局注册 Element UI
Vue.use(ElementUI)

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
//cd product_vue2/src
//npm run serve