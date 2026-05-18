# Product Management

本仓库包含图书商品管理系统的前端和后端代码。

## 目录结构

```text
backend/   Spring Boot 后端项目
frontend/  Vue2 前端项目
```

## 后端

后端代码位于 `backend/`，主要技术栈为 Spring Boot、MyBatis/MyBatis-Plus、MySQL。

常用命令：

```bash
cd backend
mvn clean package
mvn spring-boot:run
```

## 前端

前端代码位于 `frontend/`，主要技术栈为 Vue2、Vue Router、Element UI。

常用命令：

```bash
cd frontend
npm install
npm run serve
npm run build
```

## 提交说明

仓库只提交源码、配置文件和依赖声明文件，不提交本地依赖和构建产物，例如：

```text
node_modules/
target/
dist/
dist.zip
.idea/
.vscode/
```
