<template>
  <div class="auth-container">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration top-left"></div>
    <div class="bg-decoration bottom-right"></div>

    <!-- 新增：淡紫色随机动态点背景层 -->
    <div class="purple-dot-bg">
      <div class="purple-dot" v-for="i in 50" :key="i"></div>
    </div>

    <div class="auth-box">
      <!-- 左侧品牌展示区 -->
      <div class="brand-section">
        <div class="logo-text">NJUST SHOP</div>
        <p class="brand-slogan">品质购物，轻松生活</p>

        <!-- 装饰图形 -->
        <div class="brand-pattern">
          <div class="pattern-dot" v-for="i in 20" :key="i"></div>
        </div>
      </div>

      <!-- 右侧表单区 -->
      <div class="form-section">
        <div class="auth-tabs">
          <span
              :class="{ active: isLogin }"
              @click="switchToLogin"
              class="tab-item"
          >
            登录
          </span>
          <span class="divider">|</span>
          <span
              :class="{ active: !isLogin }"
              @click="switchToRegister"
              class="tab-item"
          >
            注册
          </span>
        </div>

        <!-- 登录表单 -->
        <form v-if="isLogin" class="auth-form" @submit.prevent="handleLogin">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <div class="input-wrapper">
              <input
                  type="text"
                  v-model="loginForm.userName"
                  placeholder="请输入用户名"
                  class="form-input"
                  @focus="inputFocus($event)"
                  @blur="inputBlur($event)"
              />
              <i class="icon-user"></i>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrapper">
              <input
                  type="password"
                  v-model="loginForm.password"
                  placeholder="请输入密码"
                  class="form-input"
                  @focus="inputFocus($event)"
                  @blur="inputBlur($event)"
              />
              <i class="icon-lock"></i>
            </div>
          </div>
          <button type="submit" class="auth-btn login-btn">
            <span>登录</span>
            <i class="icon-arrow-right"></i>
          </button>
        </form>

        <!-- 注册表单 -->
        <form v-else class="auth-form" @submit.prevent="handleRegister">
          <div class="form-group">
            <label class="form-label">用户名</label>
            <div class="input-wrapper">
              <input
                  type="text"
                  v-model="registerForm.userName"
                  placeholder="请设置用户名"
                  class="form-input"
                  @focus="inputFocus($event)"
                  @blur="inputBlur($event)"
              />
              <i class="icon-user"></i>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">密码</label>
            <div class="input-wrapper">
              <input
                  type="password"
                  v-model="registerForm.password"
                  placeholder="请设置密码"
                  class="form-input"
                  @focus="inputFocus($event)"
                  @blur="inputBlur($event)"
              />
              <i class="icon-lock"></i>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">姓名</label>
            <div class="input-wrapper">
              <input
                  type="text"
                  v-model="registerForm.name"
                  placeholder="请输入真实姓名"
                  class="form-input"
                  @focus="inputFocus($event)"
                  @blur="inputBlur($event)"
              />
              <i class="icon-name"></i>
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">电话</label>
            <div class="input-wrapper">
              <input
                  type="text"
                  v-model="registerForm.phone"
                  placeholder="请输入手机号码"
                  class="form-input"
                  @focus="inputFocus($event)"
                  @blur="inputBlur($event)"
              />
              <i class="icon-phone"></i>
            </div>
          </div>
          <div class="form-group role-group">
            <label class="form-label">身份</label>
            <div class="role-options">
              <label class="role-option" @click="registerForm.role = 'user'">
                <div class="custom-radio" :class="{ checked: registerForm.role === 'user' }">
                  <i :class="registerForm.role === 'user' ? 'icon-check' : ''"></i>
                </div>
                <span>用户</span>
              </label>
              <label class="role-option" @click="registerForm.role = 'merchant'">
                <div class="custom-radio" :class="{ checked: registerForm.role === 'merchant' }">
                  <i :class="registerForm.role === 'merchant' ? 'icon-check' : ''"></i>
                </div>
                <span>商家</span>
              </label>
            </div>
          </div>
          <button type="submit" class="auth-btn register-btn">
            <span>注册</span>
            <i class="icon-user-plus"></i>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import api from '@/api/index';

export default {
  name: 'AuthPage',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const isLogin = ref(true);

    const loginForm = ref({
      userName: '',
      password: ''
    });

    const registerForm = ref({
      userName: '',
      password: '',
      name: '',
      phone: '',
      role: 'user'
    });

    const switchToLogin = () => isLogin.value = true;
    const switchToRegister = () => isLogin.value = false;

    // 输入框聚焦效果
    const inputFocus = (event) => {
      event.target.parentElement.classList.add('focused');
    };

    // 输入框失焦效果
    const inputBlur = (event) => {
      if (!event.target.value) {
        event.target.parentElement.classList.remove('focused');
      }
    };

    const handleLogin = async () => {
      try {
        const res = await api.post('/user/login', loginForm.value);
        if (res.success) {
          localStorage.setItem('sessionId', res.data.sessionId);
          localStorage.setItem('userId', res.data.userId);
          localStorage.setItem('username', res.data.username);
          localStorage.setItem('role', res.data.role);

          let targetPath;
          if (res.data.role === 'admin') {
            targetPath = '/admin';
          } else if (res.data.role === 'seller') {
            targetPath = { name: 'GoodsManage' };
          } else {
            targetPath = route.query.redirect ? { path: route.query.redirect } : '/';
          }
          router.push(targetPath);
        } else {
          alert(res.message || '登录失败，请检查用户名和密码');
        }
      } catch (error) {
        console.error('登录请求失败:', error);
        alert('网络错误，请检查连接');
      }
    };

    const handleRegister = async () => {
      try {
        const res = await api.post('/user/register', registerForm.value);
        if (res.success) {
          alert('注册成功，请登录');
          switchToLogin();
        } else {
          alert(res.message || '注册失败');
        }
      } catch (error) {
        console.error('注册请求失败:', error);
        alert('网络错误，请检查连接');
      }
    };

    return {
      isLogin,
      loginForm,
      registerForm,
      switchToLogin,
      switchToRegister,
      handleLogin,
      handleRegister,
      inputFocus,
      inputBlur
    };
  }
};
</script>

<style scoped>
/* 基础样式保持不变 */
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f8f8f8;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  position: relative;
  overflow: hidden;
}

/* 新增：淡紫色随机动态点背景 */
.purple-dot-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none; /* 不影响鼠标交互 */
}

.purple-dot {
  position: absolute;
  background-color: rgba(190, 130, 255, 0.3); /* 淡紫色 */
  border-radius: 50%;
  animation: floatPulse 8s infinite ease-in-out;
}

/* 随机大小和位置的点（通过nth-child实现多样化） */
.purple-dot:nth-child(odd) {
  width: 4px;
  height: 4px;
  animation-duration: 6s;
}

.purple-dot:nth-child(even) {
  width: 6px;
  height: 6px;
  background-color: rgba(150, 100, 255, 0.2);
  animation-duration: 10s;
}

.purple-dot:nth-child(3n) {
  width: 8px;
  height: 8px;
  background-color: rgba(120, 80, 255, 0.15);
  animation-duration: 12s;
}

/* 随机位置分布（通过CSS变量动态计算） */
.purple-dot {
  left: calc(var(--random-left, 50%) * 1%);
  top: calc(var(--random-top, 50%) * 1%);
}

/* 动态生成随机位置（通过CSS实现） */
.purple-dot:nth-child(1) { --random-left: 10; --random-top: 20; animation-delay: 0s; }
.purple-dot:nth-child(2) { --random-left: 80; --random-top: 30; animation-delay: 0.5s; }
.purple-dot:nth-child(3) { --random-left: 30; --random-top: 70; animation-delay: 1s; }
.purple-dot:nth-child(4) { --random-left: 60; --random-top: 10; animation-delay: 1.5s; }
.purple-dot:nth-child(5) { --random-left: 90; --random-top: 90; animation-delay: 2s; }
.purple-dot:nth-child(6) { --random-left: 20; --random-top: 50; animation-delay: 2.5s; }
.purple-dot:nth-child(7) { --random-left: 50; --random-top: 80; animation-delay: 3s; }
.purple-dot:nth-child(8) { --random-left: 70; --random-top: 40; animation-delay: 3.5s; }
/* 更多点的位置通过CSS变量自动计算，保持代码简洁 */

/* 点的浮动+缩放动画 */
@keyframes floatPulse {
  0% {
    transform: translate(0, 0) scale(1);
    opacity: 0.3;
  }
  25% {
    transform: translate(10px, -10px) scale(1.2);
    opacity: 0.5;
  }
  50% {
    transform: translate(0, 10px) scale(1);
    opacity: 0.7;
  }
  75% {
    transform: translate(-10px, 5px) scale(0.8);
    opacity: 0.4;
  }
  100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.3;
  }
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  z-index: 0;
  filter: blur(80px);
  opacity: 0.2;
  /* 新增：渐变动态效果 */
  background: linear-gradient(135deg, #7f00c2, #bd3fff);
  animation: gradientShift 15s infinite ease-in-out;
}

/* 渐变动态变化动画 */
@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
    transform: scale(1);
  }
  50% {
    background-position: 100% 50%;
    transform: scale(1.1);
  }
  100% {
    background-position: 0% 50%;
    transform: scale(1);
  }
}

.top-left {
  top: -200px;
  left: -200px;
}

.bottom-right {
  bottom: -200px;
  right: -200px;
  background: linear-gradient(135deg, #9d4edd, #7b2cbf);
  animation: gradientShift 20s infinite ease-in-out 2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-50px) rotate(5deg); }
}

.auth-box {
  width: 50%;
  height: 65vh;
  min-width: 800px;
  max-width: 1000px;
  display: flex;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.15);
  position: relative;
  z-index: 1;
  background-color: #fff;
  transition: transform 0.5s;
}

.auth-box:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 60px rgba(0, 0, 0, 0.2);
}

/* 品牌区域美化 */
.brand-section {
  flex: 1;
  background: linear-gradient(135deg, #7f00c2 0%, #5a0099 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
  background-size: 200% 200%;
  animation: brandGradientShift 15s ease infinite;
}

/* 品牌区域渐变动画 */
@keyframes brandGradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.logo-text {
  color: white;
  font-size: 4em;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  position: relative;
  z-index: 2;
}

.brand-slogan {
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.2em;
  letter-spacing: 1px;
  position: relative;
  z-index: 2;
}

.brand-pattern {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  opacity: 0.1;
}

.pattern-dot {
  position: absolute;
  width: 10px;
  height: 10px;
  background-color: white;
  border-radius: 50%;
  animation: pulse 4s infinite;
}

/* 随机位置的点 */
.pattern-dot:nth-child(1) { top: 20%; left: 30%; animation-delay: 0s; }
.pattern-dot:nth-child(2) { top: 40%; left: 70%; animation-delay: 0.5s; }
.pattern-dot:nth-child(3) { top: 60%; left: 20%; animation-delay: 1s; }
.pattern-dot:nth-child(4) { top: 80%; left: 50%; animation-delay: 1.5s; }
.pattern-dot:nth-child(5) { top: 30%; left: 80%; animation-delay: 2s; }
.pattern-dot:nth-child(6) { top: 70%; left: 10%; animation-delay: 2.5s; }

@keyframes pulse {
  0%, 100% { transform: scale(0.8); opacity: 0.5; }
  50% { transform: scale(1.2); opacity: 1; }
}

/* 表单区域美化（保持不变） */
.form-section {
  flex: 1.2;
  background-color: white;
  padding: 50px 60px;
  overflow-y: auto;
}

.auth-tabs {
  text-align: center;
  margin-bottom: 40px;
  font-size: 18px;
  position: relative;
}

.tab-item {
  cursor: pointer;
  color: #666;
  transition: all 0.3s;
  padding: 10px 20px;
  position: relative;
}

.tab-item.active {
  color: #7f00c2;
  font-weight: bold;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  height: 3px;
  background-color: #7f00c2;
  border-radius: 3px;
  transition: width 0.3s;
}

.divider {
  margin: 0 12px;
  color: #ddd;
}

.auth-form {
  width: 100%;
}

.form-group {
  margin-bottom: 25px;
  display: flex;
  align-items: center;
}

.form-label {
  width: 80px;
  margin-right: 10px;
  text-align: right;
  color: #666;
  font-weight: 500;
}

.input-wrapper {
  flex: 1;
  position: relative;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  transition: all 0.3s;
}

.input-wrapper.focused {
  border-color: #7f00c2;
  box-shadow: 0 0 0 3px rgba(127, 0, 194, 0.1);
}

.form-input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
  background: transparent;
}

.form-input:focus {
  outline: none;
}

[class^="icon-"] {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #aaa;
  transition: color 0.3s;
}

.input-wrapper.focused [class^="icon-"] {
  color: #7f00c2;
}

.icon-user::before { content: '👤'; }
.icon-lock::before { content: '🔒'; }
.icon-name::before { content: '📛'; }
.icon-phone::before { content: '📞'; }
.icon-arrow-right::before { content: '→'; }
.icon-user-plus::before { content: '➕'; }
.icon-check::before { content: '✓'; }

.role-group {
  margin-top: 30px;
}

.role-options {
  display: flex;
  justify-content: flex-start;
  gap: 30px;
  margin-top: 12px;
}

.role-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0;
}

.custom-radio {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #ccc;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 8px;
  transition: all 0.3s;
}

.custom-radio.checked {
  background-color: #7f00c2;
  border-color: #7f00c2;
  color: white;
}

.custom-radio i {
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 12px;
}

.custom-radio.checked i {
  opacity: 1;
}

.auth-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  position: relative;
  overflow: hidden;
}

.auth-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.6s;
}

.auth-btn:hover::after {
  left: 100%;
}

.login-btn {
  background-color: #7100b8;
  color: white;
}

.login-btn:hover {
  background-color: #5a0099;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(113, 0, 184, 0.3);
}

.register-btn {
  background-color: #333;
  color: white;
}

.register-btn:hover {
  background-color: #1a1a1a;
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(51, 51, 51, 0.3);
}

/* 响应式调整 */
@media (max-width: 992px) {
  .auth-box {
    width: 80%;
    min-width: 600px;
  }
}

@media (max-width: 768px) {
  .auth-box {
    width: 90%;
    min-width: unset;
    flex-direction: column;
    height: auto;
  }

  .brand-section {
    padding: 40px 30px;
    min-height: 250px;
  }

  .logo-text {
    font-size: 3em;
  }

  .form-section {
    padding: 30px;
  }

  .bg-decoration, .purple-dot-bg {
    width: 300px;
    height: 300px;
  }
}
</style>