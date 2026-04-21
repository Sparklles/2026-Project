import Vue from 'vue'
import App from './App.vue'

// 引入 Element UI 及其 CSS
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

Vue.config.productionTip = false

// 全局注册 Element UI
Vue.use(ElementUI)

new Vue({
  render: h => h(App),
}).$mount('#app')
//cd product_vue2/src
//npm run serve
