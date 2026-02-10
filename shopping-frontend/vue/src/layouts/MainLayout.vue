<template>
  <div class="main-layout" @click="handleOutsideClick">
    <!-- 顶部导航栏 -->
    <header class="header">
      <span class="logo">
        <span class="logo-icon">🛒</span>
        <span class="logo-text">NJUST购物</span>
      </span>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="search-container">
          <input
              type="text"
              placeholder="搜索商品..."
              v-model="searchKeyword"
              @keyup.enter="handleSearch"
              @focus="searchFocus = true"
              @blur="searchFocus = false"
          >
          <button class="search-btn" @click="handleSearch">
            <i class="search-icon">🔍</i>
          </button>
        </div>
      </div>

      <div class="nav-right">
        <div class="nav-buttons-container">
          <!-- 返回按钮：仅在非首页时显示 -->
          <button
              class="nav-action-btn back-btn"
              @click="goBack"
              v-if="!isHomePage"
          >
            <i>⬅️</i>
            <span>返回</span>
          </button>

          <!-- 购物车按钮 -->
          <button class="nav-action-btn cart-btn" @click="goCart">
            <i>🛒</i>
            <span>购物车</span>
          </button>

          <!-- 用户按钮 -->
          <div class="user-menu-container" @click.stop>
            <button
                class="nav-action-btn user-btn"
                @mouseenter="showUserMenu = true"
                @mouseleave="hideUserMenuTimeout"
            >
              <div class="avatar">{{ userInitial }}</div>
              <span class="username">{{ username || '未登录' }}</span>
              <i class="user-arrow" :class="{ 'rotate': showUserMenu }">▼</i>
            </button>

            <!-- 下拉菜单 -->
            <transition name="fade">
              <div
                  class="user-menu"
                  v-show="showUserMenu"
                  @mouseenter="clearHideTimeout"
                  @mouseleave="setHideTimeout"
              >
                <button class="menu-item" @click="goProfile">
                  <i>👤</i> 个人信息
                </button>
                <button class="menu-item" @click="goOrdersManage">
                  <i>📦</i> 我的订单
                </button>
                <button class="menu-item" @click="goCommentsManage">
                  <i>💬</i> 我的评论
                </button>
                <button class="menu-item" @click="goMyGoodsManage">
                  <i>📋</i> 我的物品
                </button>
                <div class="menu-divider"></div>
                <button class="menu-item logout-item" @click="logout">
                  <i>🚪</i> 退出登录
                </button>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容区 -->
    <div class="content">
      <!-- 左侧导航 -->
      <nav class="sidebar">
        <div class="sidebar-decoration top"></div>

        <div class="menu-section" v-for="section in menuSections" :key="section.title">
          <div class="menu-header" @click="toggleSection(section)">
            <h3>
              <i :class="section.icon"></i>
              {{ section.title }}
            </h3>
            <span class="arrow" :class="{ 'rotated': section.expanded }">{{ section.expanded ? '▼' : '▶' }}</span>
          </div>
          <transition name="slide">
            <ul v-show="section.expanded">
              <li
                  v-for="item in section.items"
                  :key="item"
                  @click="selectMenuItem(item)"
                  :class="{ active: currentMenuItem === item }"
              >
                <span class="item-dot"></span>
                {{ item }}
              </li>
            </ul>
          </transition>
        </div>

        <!-- 公告展示框（左侧下方） -->
        <div class="notice-box">
          <div class="notice-header">
            <h3 class="notice-title">最新公告</h3>
            <div class="notice-glow"></div>
          </div>
          <div class="notice-content">
            <!-- 公告列表 -->
            <div class="notice-list" v-if="noticeList.length > 0">
              <div
                  class="notice-item"
                  v-for="item in noticeList"
                  :key="item.id"
                  @mouseenter="item.hover = true"
                  @mouseleave="item.hover = false"
              >
                <div class="notice-id">#{{ item.id }}</div>
                <div class="notice-text">
                  <div class="notice-title-text">{{ item.title }}</div>
                  <div class="notice-desc">{{ item.content.length > 30 ? item.content.slice(0, 30) + '...' : item.content }}</div>
                  <div class="notice-time">{{ formatTime(item.time) }}</div>
                </div>
              </div>
            </div>
            <div v-else class="notice-empty">暂无公告</div>

            <!-- 分页控件 -->
            <div class="notice-pagination" v-if="total > 0">
              <button
                  class="page-btn"
                  :disabled="currentPage === 1"
                  @click="changePage(currentPage - 1)"
              >上一页</button>
              <span class="page-info">{{ currentPage }} / {{ totalPages }}</span>
              <button
                  class="page-btn"
                  :disabled="currentPage === totalPages"
                  @click="changePage(currentPage + 1)"
              >下一页</button>
            </div>
          </div>
        </div>

        <div class="sidebar-decoration bottom"></div>
      </nav>

      <!-- 右侧内容区 -->
      <main class="main-content">
        <router-view/>
      </main>
    </div>
  </div>
</template>

<script>
import { useRouter, useRoute } from 'vue-router';
import { ref, onMounted, watch, onBeforeUnmount, computed } from 'vue';
import { getAnnouncements } from '@/api/admin';
import dayjs from 'dayjs';

export default {
  name: 'MainLayout',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const searchKeyword = ref('');
    const searchFocus = ref(false);
    const showUserMenu = ref(false);
    const currentMenuItem = ref('');
    const menuSections = ref([
      {
        title: '购物广场',
        expanded: false,
        items: ['电子产品', '生活', '美食', '服装'],
        icon: '🏬'
      },
      {
        title: '二手市场',
        expanded: false,
        items: ['二手物品', '发布'],
        icon: '♻️'
      }
    ]);

    // 判断是否为首页
    const isHomePage = computed(() => {
      return route.path === '/';
    });

    // 用户信息（从localStorage获取）
    const username = ref('');
    const userId = ref('');
    const userInitial = ref('用');
    let hideMenuTimeout = null;

    // 公告相关响应式数据
    const noticeList = ref([]);
    const currentPage = ref(1);
    const pageSize = ref(3);
    const total = ref(0);

    // 计算公告总页数
    const totalPages = computed(() => {
      return Math.ceil(total.value / pageSize.value);
    });

    // 从localStorage获取用户信息
    const fetchUserInfo = () => {
      const storedUserId = localStorage.getItem('userId');
      const storedUsername = localStorage.getItem('username');
      if (storedUserId && storedUsername) {
        userId.value = storedUserId;
        username.value = storedUsername;
        userInitial.value = storedUsername.charAt(0) || '用';
      } else {
        username.value = '';
        userId.value = '';
      }
    };

    // 获取公告列表
    const fetchNotices = async () => {
      try {
        const res = await getAnnouncements({
          page: currentPage.value,
          pageSize: pageSize.value
        });
        if (res && res.success && res.data) {
          noticeList.value = (res.data.list || []).map(item => ({
            ...item,
            hover: false
          }));
          total.value = res.data.total || 0;
        }
      } catch (error) {
        console.error('获取公告失败:', error);
      }
    };

    // 切换公告页码
    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return;
      currentPage.value = page;
      fetchNotices();
    };

    // 时间格式化
    const formatTime = (time) => {
      return time ? dayjs(time).format('MM-DD HH:mm') : '';
    };

    // 点击页面其他区域关闭用户菜单
    const handleOutsideClick = () => {
      if (showUserMenu.value) {
        showUserMenu.value = false;
        clearHideTimeout();
      }
    };

    // 生命周期钩子
    onMounted(() => {
      fetchUserInfo();
      window.addEventListener('storage', handleStorageChange);
      fetchNotices();

      // 为左侧菜单添加初始展开动画
      setTimeout(() => {
        menuSections.value[0].expanded = true;
      }, 300);
    });

    onBeforeUnmount(() => {
      window.removeEventListener('storage', handleStorageChange);
      if (hideMenuTimeout) clearTimeout(hideMenuTimeout);
    });

    const handleStorageChange = (event) => {
      if (event.key === 'userId' || event.key === 'username' || event.key === 'sessionId') {
        fetchUserInfo();
      }
    };

    // 监听路由变化
    watch(
        () => route.params.type,
        (newType) => {
          if (newType) {
            menuSections.value.forEach(section => {
              if (section.items.includes(newType)) {
                section.expanded = true;
                currentMenuItem.value = newType;
              }
            });
          }
        },
        { immediate: true }
    );

    // 切换菜单展开/折叠
    const toggleSection = (section) => {
      section.expanded = !section.expanded;
    };

    // 点击左侧菜单项
    const selectMenuItem = (item) => {
      currentMenuItem.value = item;

      if (item === '发布') {
        if (!userId.value) {
          alert('请先登录才能发布商品');
          router.push({ name: 'Auth' });
          return;
        }
        router.push({ name: 'Publish' });
      } else {
        router.push({
          name: 'Category',
          params: { type: item }
        });
      }
    };

    // 搜索功能
    const handleSearch = () => {
      if (!searchKeyword.value.trim()) return;
      router.push({
        name: 'SearchResult',
        query: { keyword: searchKeyword.value.trim() }
      });
    };

    // 顶部导航按钮逻辑
    const goBack = () => {
      router.go(-1);
    };

    const goCart = () => {
      if (!userId.value) {
        alert('请先登录');
        router.push({ name: 'Auth' });
        return;
      }
      router.push({ name: 'ShoppingCart' });
    };

    const goOrdersManage = () => {
      if (!userId.value) {
        alert('请先登录');
        router.push({ name: 'Auth' });
        return;
      }
      router.push({ name: 'OrdersManage' });
    };

    const goCommentsManage = () => {
      router.push({ name: 'CommentsManage' });
    }

    const goMyGoodsManage = () => {
      router.push({ name: 'MyGoodsManage' });
    }

    const goProfile = () => {
      if (!userId.value) {
        alert('请先登录');
        router.push({ name: 'Auth' });
        return;
      }
      router.push({ name: 'UserProfile' });
    };

    // 退出登录
    const logout = () => {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('sessionId');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        username.value = '';
        userId.value = '';
        showUserMenu.value = false; // 退出登录时关闭菜单
        router.push({ name: 'Auth' });
        alert('已退出登录!');
      }
    };

    // 控制用户菜单显示/隐藏
    const setHideTimeout = () => {
      hideMenuTimeout = setTimeout(() => {
        showUserMenu.value = false;
      }, 300);
    };

    const clearHideTimeout = () => {
      if (hideMenuTimeout) {
        clearTimeout(hideMenuTimeout);
        hideMenuTimeout = null;
      }
    };

    const hideUserMenuTimeout = () => {
      setHideTimeout();
    };

    return {
      searchKeyword,
      searchFocus,
      showUserMenu,
      menuSections,
      currentMenuItem,
      toggleSection,
      selectMenuItem,
      handleSearch,
      goBack,
      goCart,
      goOrdersManage,
      goCommentsManage,
      goMyGoodsManage,
      username,
      userId,
      userInitial,
      goProfile,
      logout,
      hideUserMenuTimeout,
      clearHideTimeout,
      noticeList,
      currentPage,
      totalPages,
      changePage,
      formatTime,
      isHomePage,
      handleOutsideClick // 暴露点击事件处理方法
    };
  }
};
</script>

<style scoped>
/* 样式保持不变 */
/* 基础样式 */
.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  font-family: 'PingFang SC', sans-serif;
  background-color: #f9f9f9;
  background-image:
      radial-gradient(#e6e6fa 0.5px, transparent 0.5px),
      radial-gradient(#e6e6fa 0.5px, #f9f9f9 0.5px);
  background-size: 20px 20px;
  background-position: 0 0, 10px 10px;
}

/* 顶部导航栏 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 5%;
  border-bottom: 1px solid #eee;
  width: 100%;
  box-sizing: border-box;
  background-color: white;
  z-index: 100;
  position: sticky;
  top: 0;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s;
}

.header:hover {
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #8a2be2;
  font-size: 20px;
  font-weight: bold;
  cursor: pointer;
  transition: transform 0.3s;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-icon {
  font-size: 24px;
}

.search-bar {
  display: flex;
  width: 40%;
  margin: 0 20px;
}

.search-container {
  flex: 1;
  position: relative;
  transition: all 0.3s;
}

.search-bar input {
  width: 100%;
  padding: 10px 15px 10px 40px;
  border: 1px solid #ddd;
  border-radius: 24px;
  font-size: 14px;
  transition: all 0.3s;
}

.search-bar input:focus {
  outline: none;
  border-color: #8a2be2;
  box-shadow: 0 0 0 3px rgba(138, 43, 226, 0.1);
  padding-left: 45px;
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #999;
  transition: all 0.3s;
}

.search-container:focus-within .search-icon {
  color: #8a2be2;
  left: 12px;
}

.search-btn {
  position: absolute;
  right: 5px;
  top: 50%;
  transform: translateY(-50%);
  padding: 6px 15px;
  background-color: #8a2be2;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.search-btn:hover {
  background-color: #7b1fa2;
  transform: translateY(-50%) scale(1.05);
}

.nav-buttons-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 40px;
  padding: 0 15px;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid #ddd;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: white;
}

.nav-action-btn:hover {
  border-color: #8a2be2;
  color: #8a2be2;
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(138, 43, 226, 0.1);
}

.back-btn i {
  font-style: normal;
}

.cart-btn {
  position: relative;
  overflow: hidden;
}

.cart-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.1), transparent);
  transition: left 0.6s;
}

.cart-btn:hover::after {
  left: 100%;
}

.user-menu-container {
  position: relative;
  display: inline-block;
}

.user-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.user-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.1), transparent);
  transition: left 0.6s;
}

.user-btn:hover::after {
  left: 100%;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: #8a2be2;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: bold;
  box-shadow: 0 2px 5px rgba(138, 43, 226, 0.2);
  transition: transform 0.3s;
}

.user-btn:hover .avatar {
  transform: scale(1.1) rotate(5deg);
}

.user-arrow {
  font-size: 12px;
  transition: transform 0.3s;
}

.user-arrow.rotate {
  transform: rotate(180deg);
}

.user-menu {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 5px;
  width: 180px;
  background-color: white;
  border: 1px solid #eee;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  z-index: 100;
  overflow: hidden;
  transform-origin: top right;
  animation: menuPop 0.2s ease-out;
}

@keyframes menuPop {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 15px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.menu-item:hover {
  background-color: #f0e6ff;
  color: #8a2be2;
  padding-left: 20px;
}

.menu-divider {
  height: 1px;
  background-color: #f0f0f0;
  margin: 5px 0;
}

.logout-item {
  color: #e53935;
}

.logout-item:hover {
  background-color: #ffebee;
  color: #e53935;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.98);
}

/* 左侧导航栏 */
.sidebar {
  width: 220px;
  padding: 20px 0;
  border-right: 1px solid #eee;
  background-color: white;
  height: calc(100vh - 65px);
  position: sticky;
  top: 65px;
  overflow-y: auto;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.03);
  z-index: 50;
}

.sidebar-decoration {
  position: absolute;
  width: 100%;
  height: 40px;
  background: linear-gradient(to bottom, rgba(138, 43, 226, 0.05), transparent);
  pointer-events: none;
}

.sidebar-decoration.top {
  top: 0;
}

.sidebar-decoration.bottom {
  bottom: 0;
  background: linear-gradient(to top, rgba(138, 43, 226, 0.05), transparent);
}

.menu-section {
  margin-bottom: 10px;
  padding: 0 15px;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  cursor: pointer;
  border-radius: 8px;
  margin-bottom: 5px;
  transition: all 0.3s;
}

.menu-header:hover {
  background-color: #f5f3ff;
}

.menu-header h3 {
  margin: 0;
  font-size: 15px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 10px;
}

.arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s;
}

.arrow.rotated {
  transform: rotate(90deg);
  color: #8a2be2;
}

.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow: hidden;
}

.sidebar li {
  padding: 10px 15px 10px 40px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  color: #666;
  border-left: 3px solid transparent;
}

.sidebar li:hover {
  background-color: #f5f3ff;
  color: #8a2be2;
  padding-left: 45px;
}

.sidebar li.active {
  background-color: #f0e6ff;
  color: #8a2be2;
  font-weight: 500;
  border-left-color: #8a2be2;
}

.item-dot {
  position: absolute;
  left: 25px;
  top: 50%;
  transform: translateY(-50%);
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #ddd;
  transition: all 0.3s;
}

.sidebar li:hover .item-dot {
  background-color: #c0a4e8;
  transform: translateY(-50%) scale(1.2);
}

.sidebar li.active .item-dot {
  background-color: #8a2be2;
}

.slide-enter-active, .slide-leave-active {
  transition: max-height 0.3s ease, opacity 0.3s ease;
}

.slide-enter-from, .slide-leave-to {
  max-height: 0;
  opacity: 0;
}

.slide-enter-to, .slide-leave-from {
  max-height: 300px;
  opacity: 1;
}

/* 公告展示框 */
.notice-box {
  margin: 25px 15px 15px;
  padding: 15px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  position: relative;
}

.notice-header {
  position: relative;
  margin-bottom: 15px;
}

.notice-title {
  font-size: 16px;
  margin: 0 0 10px 0;
  color: #8a2be2;
  font-weight: bold;
  padding-left: 5px;
  display: inline-block;
}

.notice-glow {
  position: absolute;
  height: 4px;
  width: 40px;
  background-color: rgba(138, 43, 226, 0.2);
  border-radius: 2px;
  bottom: 0;
  left: 5px;
}

.notice-content {
  position: relative;
}

.notice-list {
  max-height: 220px;
  overflow: hidden;
}

.notice-item {
  padding: 12px 10px;
  border-bottom: 1px dashed #f0f0f0;
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 6px;
  margin-bottom: 5px;
  position: relative;
  overflow: hidden;
}

.notice-item::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(138, 43, 226, 0.05), transparent);
  transition: left 0.5s;
}

.notice-item:hover::after {
  left: 100%;
}

.notice-item:hover {
  background-color: #faf8ff;
  transform: translateX(5px);
  border-color: #e9e4f7;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-id {
  font-size: 12px;
  color: #8a2be2;
  font-weight: bold;
  min-width: 30px;
}

.notice-text {
  flex: 1;
}

.notice-title-text {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #333;
  transition: color 0.3s;
}

.notice-item:hover .notice-title-text {
  color: #8a2be2;
}

.notice-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  display: inline-block;
}

.notice-empty {
  text-align: center;
  padding: 30px 0;
  font-size: 13px;
  color: #999;
  background-color: #fafafa;
  border-radius: 8px;
}

.notice-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 15px;
  padding-top: 5px;
}

.page-btn {
  padding: 5px 12px;
  font-size: 13px;
  border: 1px solid #eee;
  border-radius: 6px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  border-color: #8a2be2;
  color: #8a2be2;
  background-color: #f5f3ff;
}

.page-btn:disabled {
  cursor: not-allowed;
  color: #ccc;
  background: #fafafa;
}

.page-info {
  font-size: 13px;
  color: #666;
}

/* 整体布局 */
.content {
  display: flex;
  flex: 1;
}

.main-content {
  flex: 1;
  padding: 25px;
  transition: all 0.3s;
}

/* 滚动条美化 */
.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: #f5f5f5;
}

.sidebar::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.sidebar::-webkit-scrollbar-thumb:hover {
  background: #c0a4e8;
}

/* 响应式调整 */
@media (max-width: 992px) {
  .search-bar {
    width: 35%;
  }

  .sidebar {
    width: 200px;
  }
}

@media (max-width: 768px) {
  .header {
    padding: 10px 3%;
  }

  .logo-text {
    display: none;
  }

  .search-bar {
    width: 50%;
    margin: 0 10px;
  }

  .nav-action-btn span {
    display: none;
  }

  .nav-action-btn {
    min-width: auto;
    padding: 0 10px;
    width: 40px;
    justify-content: center;
  }

  .sidebar {
    width: 60px;
    padding: 15px 0;
  }

  .menu-header h3 span,
  .menu-header .arrow,
  .sidebar li span:not(.item-dot),
  .notice-box {
    display: none;
  }

  .menu-header {
    justify-content: center;
    padding: 10px;
  }

  .menu-header h3 {
    justify-content: center;
  }

  .sidebar li {
    padding: 10px;
    display: flex;
    justify-content: center;
  }

  .item-dot {
    left: 50%;
    transform: translateX(-50%) translateY(-50%);
  }

  .sidebar li:hover .item-dot {
    transform: translateX(-50%) translateY(-50%) scale(1.5);
  }
}
</style>