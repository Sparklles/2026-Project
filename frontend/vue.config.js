//设置前端项目运行端口和后端代理
const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack: config => {
    config.plugin('html').tap(args => {
      args[0].title = '航海时代'
      return args
    })
  },
  devServer: {
    port: 80,
    proxy: 'http://localhost:8083'
  }
})
