<template>
  <div id="app">
    <router-view></router-view>

    <!-- 🌟 在这里引入AI 悬浮客服组件 -->
    <AIChatWidget />
  </div>
</template>

<script>
import AIChatWidget from '@/components/AIChatWidget.vue';
export default {
  name: 'App',
  components: {
    AIChatWidget
  }
}
</script>

<style>
/* 全局样式重置与基础配置 */
body {
  margin: 0;
  padding: 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}

#app {
  height: 100vh;
}
</style>
