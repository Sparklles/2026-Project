<template>
  <div class="ai-chat-widget">
    <div
      class="chat-trigger"
      @click="toggleChat"
      :class="{ 'is-active': isOpen }"
    >
      <el-badge :is-dot="hasUnread" class="trigger-badge">
        <div class="trigger-icon-wrap">
          <i class="el-icon-service" v-if="!isOpen"></i>
          <i class="el-icon-close" v-else></i>
        </div>
      </el-badge>
    </div>

    <transition name="el-zoom-in-bottom">
      <div class="chat-panel" v-show="isOpen">
        <div class="chat-header">
          <div class="header-left">
            <el-avatar
              :size="36"
              src="https://img.alicdn.com/tfs/TB1L5vQoaL0gK0jSZFtXXXQCXXa-1000-150.png"
              class="ai-avatar"
            ></el-avatar>
            <div class="ai-info">
              <span class="ai-name"
                >深蓝小助手
                <el-tag size="mini" effect="dark" type="warning" class="ai-tag"
                  >大模型驱动</el-tag
                ></span
              >
              <span class="ai-status">航海时代首席智能导购</span>
            </div>
          </div>
          <div class="header-actions">
            <i class="el-icon-minus" @click="toggleChat" title="最小化"></i>
          </div>
        </div>

        <div class="chat-body" ref="chatBody">
          <div class="message-list">
            <div class="message-item ai">
              <el-avatar
                :size="32"
                icon="el-icon-ship"
                class="msg-avatar ai-bg"
              ></el-avatar>
              <div class="msg-content">
                <div class="msg-bubble">
                  船长您好！我是接入了 <b>智谱大模型</b> 与
                  <b>DeepSeek</b> 的深蓝小助手 🌊<br /><br />
                  您可以直接对我说：<br />
                  <span class="highlight">"找点老人坐船防晕的装备"</span><br />
                  <span class="highlight">"猜你喜欢"</span>
                </div>
                <div class="quick-replies">
                  <el-button
                    size="mini"
                    round
                    plain
                    @click="sendQuickMsg('猜你喜欢')"
                    >❤️ 猜你喜欢</el-button
                  >
                  <el-button
                    size="mini"
                    round
                    plain
                    @click="sendQuickMsg('找点适合新手的导航仪')"
                    >🧭 找新手导航仪</el-button
                  >
                  <el-button
                    size="mini"
                    round
                    plain
                    @click="sendQuickMsg('查询最新订单')"
                    >📦 查订单</el-button
                  >
                </div>
              </div>
            </div>

            <div
              class="message-item"
              v-for="(msg, index) in messages"
              :key="index"
              :class="msg.role"
            >
              <el-avatar
                :size="32"
                :icon="
                  msg.role === 'ai' ? 'el-icon-ship' : 'el-icon-user-solid'
                "
                :class="['msg-avatar', msg.role === 'ai' ? 'ai-bg' : 'user-bg']"
              ></el-avatar>

              <div class="msg-content">
                <div
                  class="msg-bubble"
                  v-if="msg.content"
                  v-html="formatMessage(msg.content)"
                ></div>

                <div
                  class="smart-card recommend-card"
                  v-if="msg.type === 'recommend'"
                >
                  <div class="card-title">
                    <i class="el-icon-magic-stick"></i> 为您精准匹配以下装备
                  </div>
                  <div class="recommend-list">
                    <div
                      class="rec-item"
                      v-for="book in msg.data"
                      :key="book.id"
                    >
                      <div class="rec-info">
                        <span class="rec-title">{{ book.title }}</span>
                        <span class="rec-price">¥{{ book.price }}</span>
                      </div>
                      <div class="rec-actions">
                        <el-button
                          type="text"
                          size="mini"
                          @click="goToProduct(book.id)"
                          >去看看</el-button
                        >
                        <el-button
                          type="warning"
                          size="mini"
                          plain
                          round
                          @click="fetchReviewSummary(book.id, book.title)"
                          >AI 一键读评</el-button
                        >
                      </div>
                    </div>
                  </div>
                </div>

                <div
                  class="smart-card review-summary-card"
                  v-if="msg.type === 'review'"
                >
                  <div class="card-title">
                    <i class="el-icon-s-data"></i> 【{{ msg.bookTitle }}】AI
                    口碑总结
                    <span
                      class="score"
                      :class="msg.data.sentimentScore >= 80 ? 'good' : 'bad'"
                    >
                      {{ msg.data.sentimentScore }}分
                    </span>
                  </div>

                  <div class="review-tags">
                    <div
                      class="pros"
                      v-if="msg.data.pros && msg.data.pros.length > 0"
                    >
                      <span class="tag-label">优点</span>
                      <el-tag
                        size="mini"
                        type="success"
                        v-for="(pro, i) in msg.data.pros"
                        :key="'p' + i"
                        class="custom-tag"
                        >{{ pro }}</el-tag
                      >
                    </div>
                    <div
                      class="cons"
                      v-if="msg.data.cons && msg.data.cons.length > 0"
                    >
                      <span class="tag-label">缺点</span>
                      <el-tag
                        size="mini"
                        type="danger"
                        v-for="(con, i) in msg.data.cons"
                        :key="'c' + i"
                        class="custom-tag"
                        >{{ con }}</el-tag
                      >
                    </div>
                  </div>

                  <div class="review-advise">
                    <strong>老船长建议：</strong
                    >{{ msg.data.comprehensiveSummary }}
                  </div>
                </div>
              </div>
            </div>

            <div class="message-item ai" v-if="isTyping">
              <el-avatar
                :size="32"
                icon="el-icon-ship"
                class="msg-avatar ai-bg"
              ></el-avatar>
              <div class="msg-content">
                <div class="msg-bubble typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="chat-footer">
          <el-input
            type="textarea"
            :rows="2"
            placeholder="想找什么装备？支持模糊描述哦..."
            v-model="inputText"
            resize="none"
            class="chat-input"
            @keyup.enter.native.prevent="sendMessage"
          ></el-input>
          <div class="footer-tools">
            <el-tooltip content="支持自然语言检索与下单" placement="top">
              <i class="el-icon-magic-stick tool-icon"></i>
            </el-tooltip>
            <el-button
              type="primary"
              size="small"
              round
              class="send-btn"
              :loading="isTyping"
              @click="sendMessage"
            >
              发送 <i class="el-icon-s-promotion"></i>
            </el-button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: "AIChatWidget",
  data() {
    return {
      isOpen: false,
      hasUnread: false,
      isTyping: false,
      inputText: "",
      sessionId: "session_" + Date.now(),
      messages: [],
    };
  },
  methods: {
    toggleChat() {
      this.isOpen = !this.isOpen;
      if (this.isOpen) {
        this.hasUnread = false;
        this.scrollToBottom();
      }
    },
    sendQuickMsg(text) {
      this.inputText = text;
      this.sendMessage();
    },

    // ==========================================
    // 🌟 全新升级的智能意图调度中心 (Agent)
    // ==========================================
    async sendMessage() {
      if (!this.inputText.trim()) return;

      const userMsg = this.inputText.trim();
      this.messages.push({ role: "user", content: userMsg });
      this.inputText = "";
      this.scrollToBottom();
      this.isTyping = true;

      try {
        // ----------------------------------------------------
        // 🎯 意图 1：独立拦截【查询订单】
        // ----------------------------------------------------
        if (
          userMsg.includes("查订单") ||
          userMsg.includes("最新订单") ||
          userMsg.includes("历史订单")
        ) {
          const userInfo = JSON.parse(localStorage.getItem("user") || "{}");
          const userId =
            userInfo.id || userInfo.userId || "2049811061216985090";

          const res = await request.get("/api/order/list/all", {
            params: { userId: userId },
          });

          // 🌟 终极安全取值：如果拦截器直接返回了数组就用 res，如果返回了完整对象就用 res.data
          const orderList = Array.isArray(res) ? res : res.data || [];

          if (orderList && orderList.length > 0) {
            // 取出最新的一条订单渲染
            const latestOrder = orderList[0];
            this.messages.push({
              role: "ai",
              content: `帮您查到啦！您的最新订单号是：<b>${
                latestOrder.orderNo
              }</b><br>金额：<span style="color:#ff5000;font-weight:bold;">¥${
                latestOrder.payPrice || latestOrder.totalPrice
              }</span><br>状态：已记录。您可以前往个人中心查看详情。`,
            });
          } else {
            this.messages.push({
              role: "ai",
              content:
                "船长，您目前还没有任何下单记录哦！赶快去选购一些装备吧。",
            });
          }
        }
        // ----------------------------------------------------
        // 🎯 意图 2：带有上下文记忆的“猜你喜欢”
        // ----------------------------------------------------
        else if (userMsg === "猜你喜欢") {
          const recentContext = this.messages
            .filter((m) => m.role === "user" && m.content !== "猜你喜欢")
            .slice(-3)
            .map((m) => m.content)
            .join("，");
          let res = recentContext
            ? await request.get("/api/ai/recommend/search", {
                params: { query: `用户特征：[${recentContext}]。请推荐商品。` },
              })
            : await request.get("/api/ai/recommend/guess-you-like");
          this.handleRecommendResponse(res);
        }
        // ----------------------------------------------------
        // 🎯 意图 3：下单/聊天意图 (交给底层 Spring AI)
        // ----------------------------------------------------
        else if (
          userMsg.includes("下单") ||
          userMsg.includes("购买") ||
          userMsg.includes("买")
        ) {
          const res = await request.post("/api/ai/chat", {
            sessionId: this.sessionId,
            message: userMsg,
          });
          const replyContent =
            res && res.data ? res.data : res.message ? res.message : res;
          this.messages.push(this.parseStandardAIResponse(replyContent));
        }
        // ----------------------------------------------------
        // 🎯 意图 4：默认搜索意图 (找书、找装备)
        // ----------------------------------------------------
        else {
          const res = await request.get("/api/ai/recommend/search", {
            params: { query: userMsg },
          });
          if (res.data && res.data.length > 0) {
            this.handleRecommendResponse(res);
          } else {
            // 搜索不到时的兜底聊天
            const chatRes = await request.post("/api/ai/chat", {
              sessionId: this.sessionId,
              message: `用户刚才搜索了商品"${userMsg}"但数据库没查到，请委婉地推荐一下基础航海书籍。`,
            });
            const chatReply =
              chatRes && chatRes.data
                ? chatRes.data
                : chatRes.message
                ? chatRes.message
                : chatRes;
            this.messages.push({
              role: "ai",
              content:
                typeof chatReply === "string"
                  ? chatReply
                  : "抱歉船长，没查到相关物品。",
            });
          }
        }
        if (!this.isOpen) this.hasUnread = true;
      } catch (error) {
        console.error("AI 接口调用异常:", error);
        this.messages.push({
          role: "ai",
          content: "抱歉船长，海上风浪太大信号受到了磁场干扰，请稍后再试。",
        });
      } finally {
        this.isTyping = false;
        this.scrollToBottom();
      }
    },

    // 处理商品卡片渲染 (增强了空值判定)
    // 处理商品卡片渲染 (增强了拦截器兼容判定)
    handleRecommendResponse(res) {
      // 🌟 同样安全取值：兼容 Axios 拦截器剥壳
      const books = Array.isArray(res) ? res : res.data || [];

      if (books && books.length > 0) {
        this.messages.push({
          role: "ai",
          content:
            "老船长为您在海量库中进行了多维语义匹配，发现这些宝贝非常符合您的心意：",
          type: "recommend",
          data: books,
        });
      } else {
        this.messages.push({
          role: "ai",
          content:
            "抱歉船长，按您目前的描述，船舱里暂时没有找到完全匹配的装备。您可以换个更具体的词，或者点一下【猜你喜欢】看看别的~",
        });
      }
    },

    // AI 读评核心接口
    // AI 读评核心接口
    async fetchReviewSummary(bookId, bookTitle) {
      this.messages.push({
        role: "user",
        content: `请帮我用 AI 总结一下【${bookTitle}】的口碑如何？`,
      });
      this.scrollToBottom();
      this.isTyping = true;

      try {
        const res = await request.get(`/api/review/summary/${bookId}`);

        // 🌟 核心修复：兼容 Axios 拦截器的脱壳行为。如果有 data 就取 data，没有就说明 res 已经是真实数据了
        const actualData = res && res.data !== undefined ? res.data : res;

        // 判断大模型是否真的返回了有效数据
        if (
          actualData &&
          actualData !== "暂无 AI 总结数据" &&
          actualData !== ""
        ) {
          try {
            // 如果后端返回的是 JSON 字符串，先解析它；如果已经是对象，直接用
            const summaryData =
              typeof actualData === "string"
                ? JSON.parse(actualData)
                : actualData;

            this.messages.push({
              role: "ai",
              type: "review",
              bookTitle: bookTitle,
              data: summaryData,
            });
          } catch (parseError) {
            console.error(
              "JSON 解析失败，后端返回的可能不是标准格式:",
              actualData
            );
            this.messages.push({
              role: "ai",
              content: "AI 生成的报告格式有些乱码，请稍后再试哦。",
            });
          }
        } else {
          this.messages.push({
            role: "ai",
            content:
              "因为该商品目前的真实评价过少，为保证客观，AI 大脑暂时无法为您生成评测报告。",
          });
        }
      } catch (error) {
        console.error("AI 读评异常:", error);
        this.messages.push({
          role: "ai",
          content: "读取 AI 评价报告失败，可能是后端大模型正在打盹。",
        });
      } finally {
        this.isTyping = false;
        this.scrollToBottom();
      }
    },

    // 正则提取后端兜底生成的假订单 (保留你的旧功能)
    parseStandardAIResponse(text) {
      if (typeof text !== "string")
        return { role: "ai", content: "系统异常，返回格式错误" };

      let msgObj = { role: "ai", content: text, orderNo: null };
      const orderMatch = text.match(/订单号(?:为|是)?[:：]?\s*(\d{18,20})/);
      if (orderMatch && orderMatch[1]) {
        msgObj.orderNo = orderMatch[1];
      }
      return msgObj;
    },

    formatMessage(text) {
      if (!text) return "";
      let formatted = text.replace(/\n/g, "<br>");
      formatted = formatted.replace(
        /(¥\s*\d+(\.\d+)?)/g,
        '<span style="color:#ff5000;font-weight:bold;">$1</span>'
      );
      return formatted;
    },

    goToProduct(id) {
      this.$router.push({ name: "ProductDetail", params: { id: id } });
      this.isOpen = false;
    },
    goToPay(orderNo) {
      this.$router.push({
        path: "/pay",
        query: { orderNo: orderNo, amount: "0.00" },
      });
      this.isOpen = false;
    },

    scrollToBottom() {
      this.$nextTick(() => {
        if (this.$refs.chatBody) {
          this.$refs.chatBody.scrollTop = this.$refs.chatBody.scrollHeight;
        }
      });
    },
  },
};
</script>

<style scoped>
/* =========== CSS 样式保持之前的原样不变，确保视觉完美 =========== */
.ai-chat-widget {
  position: fixed;
  right: 40px;
  bottom: 40px;
  z-index: 9999;
  font-family: "Helvetica Neue", Helvetica, sans-serif;
}
.chat-trigger {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #1890ff, #0050b3);
  border-radius: 50%;
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
}
.chat-trigger:hover {
  transform: translateY(-5px) scale(1.05);
}
.chat-trigger.is-active {
  transform: rotate(90deg) scale(0);
  opacity: 0;
  pointer-events: none;
}
.trigger-icon-wrap {
  color: #fff;
  font-size: 28px;
}
.chat-panel {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 400px;
  height: 650px;
  background: #f4f7f9;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transform-origin: bottom right;
}
.chat-header {
  background: linear-gradient(135deg, #003366, #1890ff);
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ai-avatar {
  border: 2px solid rgba(255, 255, 255, 0.3);
}
.ai-info {
  display: flex;
  flex-direction: column;
}
.ai-name {
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}
.ai-tag {
  background-color: #ff9900;
  border: none;
  border-radius: 4px;
  font-weight: normal;
}
.ai-status {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 2px;
}
.header-actions i {
  font-size: 18px;
  cursor: pointer;
  padding: 5px;
  transition: opacity 0.2s;
}
.header-actions i:hover {
  opacity: 0.7;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background-color: #f4f7f9;
}
.message-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  max-width: 95%;
}
.message-item.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}
.msg-avatar.ai-bg {
  background-color: #1890ff;
  color: #fff;
}
.msg-avatar.user-bg {
  background-color: #ff5000;
  color: #fff;
}
.msg-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.message-item.user .msg-content {
  align-items: flex-end;
}
.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  word-break: break-all;
}
.message-item.ai .msg-bubble {
  background-color: #fff;
  border-top-left-radius: 2px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}
.message-item.user .msg-bubble {
  background: linear-gradient(90deg, #ff9000, #ff5000);
  color: #fff;
  border-top-right-radius: 2px;
  box-shadow: 0 2px 8px rgba(255, 80, 0, 0.2);
}
.highlight {
  color: #1890ff;
  font-weight: bold;
}
.quick-replies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 5px;
}

/* 智能卡片 */
.smart-card {
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 14px;
  width: 260px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.card-title {
  font-size: 14px;
  font-weight: bold;
  color: #333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}
.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.rec-item {
  display: flex;
  flex-direction: column;
  padding-bottom: 10px;
  border-bottom: 1px dashed #eee;
}
.rec-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}
.rec-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.rec-title {
  font-size: 13px;
  color: #333;
  width: 150px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rec-price {
  font-size: 14px;
  font-weight: bold;
  color: #ff5000;
}
.rec-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.review-summary-card {
  width: 280px;
  background: linear-gradient(to bottom, #f0f7ff, #ffffff);
  border: 1px solid #cce4ff;
}
.review-summary-card .card-title {
  color: #0050b3;
  border-bottom: 1px solid #e6f1fc;
  padding-bottom: 8px;
  justify-content: space-between;
}
.score {
  font-size: 16px;
  font-weight: 900;
}
.score.good {
  color: #52c41a;
}
.score.bad {
  color: #f5222d;
}
.review-tags {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pros,
.cons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
}
.tag-label {
  font-size: 12px;
  color: #666;
  font-weight: bold;
  margin-right: 4px;
}
.custom-tag {
  border-radius: 12px;
}
.review-advise {
  margin-top: 15px;
  font-size: 13px;
  line-height: 1.6;
  color: #444;
  background: rgba(24, 144, 255, 0.05);
  padding: 10px;
  border-radius: 6px;
}

.chat-footer {
  background: #fff;
  padding: 15px;
  border-top: 1px solid #eee;
}
.chat-input ::v-deep .el-textarea__inner {
  border: none;
  background: #f9f9f9;
  border-radius: 8px;
  padding: 10px;
  font-size: 13px;
}
.chat-input ::v-deep .el-textarea__inner:focus {
  box-shadow: 0 0 0 1px #1890ff;
}
.footer-tools {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}
.tool-icon {
  font-size: 20px;
  color: #1890ff;
  cursor: pointer;
}
.send-btn {
  background-color: #1890ff;
  border-color: #1890ff;
}
.typing-indicator span {
  display: inline-block;
  width: 6px;
  height: 6px;
  background-color: #ccc;
  border-radius: 50%;
  margin: 0 2px;
  animation: typing 1.4s infinite ease-in-out;
}
.typing-indicator span:nth-child(1) {
  animation-delay: 0s;
}
.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}
.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}
@keyframes typing {
  0%,
  80%,
  100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}
.chat-body::-webkit-scrollbar {
  width: 6px;
}
.chat-body::-webkit-scrollbar-thumb {
  background: #dcdcdc;
  border-radius: 4px;
}
</style>