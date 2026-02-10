<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <span class="logo">NJUST商家管理</span>

      <div class="nav-right">
        <div class="nav-buttons-container">
          <!-- 返回按钮：仅在非商品管理首页时显示 -->

          <!-- 用户按钮 -->
          <div class="user-menu-container">
            <button
                class="nav-action-btn user-btn"
                @mouseenter="showUserMenu = true"
                @mouseleave="hideUserMenuTimeout"
            >
              <div class="avatar">{{ userInitial }}</div>
              <span class="username">{{ username || '未登录' }}</span>
            </button>

            <!-- 下拉菜单（仅保留个人信息和退出登录） -->
            <transition name="fade">
              <div
                  class="user-menu"
                  v-show="showUserMenu"
                  @mouseenter="clearHideTimeout"
                  @mouseleave="setHideTimeout"
              >
                <button class="menu-item" @click="goProfile">个人信息</button>
                <button class="menu-item" @click="logout">退出登录</button>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容区 -->
    <div class="content">
      <!-- 左侧导航（增加商品发布） -->
      <nav class="sidebar">
        <div class="menu-section">
          <div class="menu-header" @click="toggleSection(merchantSection)">
            <h3>{{ merchantSection.title }}</h3>
            <span class="arrow">{{ merchantSection.expanded ? '▼' : '▶' }}</span>
          </div>
          <transition name="slide">
            <ul v-show="merchantSection.expanded">
              <li
                  v-for="item in merchantSection.items"
                  :key="item"
                  @click="selectMenuItem(item)"
                  :class="{ active: currentMenuItem === item }"
              >
                {{ item }}
              </li>
            </ul>
          </transition>
        </div>
      </nav>

      <!-- 右侧内容区 -->
      <main class="main-content">
        <router-view/> <!-- 路由出口，显示匹配的页面组件 -->
      </main>
    </div>
  </div>
</template>

<script>
import { useRouter, useRoute } from 'vue-router';
import { ref, onMounted, watch, onBeforeUnmount, computed } from 'vue';

export default {
  name: 'MerchantLayout',
  setup() {
    const router = useRouter();
    const route = useRoute();
    const showUserMenu = ref(false);
    const currentMenuItem = ref('');

    // 左侧导航增加"商品发布"
    const merchantSection = ref({
      title: '商家管理',
      expanded: true,
      items: ['订单管理', '商品管理', '商品发布'] // 新增"商品发布"
    });

    // 新增：判断是否为商品管理首页（/merchant/goodsManage）
    const isGoodsManageHome = computed(() => {
      return route.path === '/merchant/goodsmanage';
    });

    // 用户信息
    const username = ref('');
    const userId = ref('');
    const userInitial = ref('商');
    let hideMenuTimeout = null;

    // 从本地存储获取用户信息
    const fetchUserInfo = () => {
      const storedUserId = localStorage.getItem('userId');
      const storedUsername = localStorage.getItem('username');
      if (storedUserId && storedUsername) {
        userId.value = storedUserId;
        username.value = storedUsername;
        userInitial.value = storedUsername.charAt(0) || '商';
      } else {
        username.value = '';
        userId.value = '';
      }
    };

    // 生命周期钩子
    onMounted(() => {
      fetchUserInfo();
      window.addEventListener('storage', handleStorageChange);
    });

    onBeforeUnmount(() => {
      window.removeEventListener('storage', handleStorageChange);
    });

    const handleStorageChange = (event) => {
      if (event.key === 'userId' || event.key === 'username') {
        fetchUserInfo();
      }
    };

    // 监听路由变化，同步菜单选中状态（增加商品发布的路由匹配）
    watch(
        () => route.path,
        (newPath) => {
          if (newPath === '/merchant/goodsmanage') {
            currentMenuItem.value = '商品管理';
          } else if (newPath === '/merchant/ordersmanagemerchant') {
            currentMenuItem.value = '订单管理';
          } else if (newPath === '/publishmerchant') { // 新增商品发布的路由匹配
            currentMenuItem.value = '商品发布';
          }
        },
        { immediate: true }
    );

    // 切换菜单展开/折叠
    const toggleSection = (section) => {
      section.expanded = !section.expanded;
    };

    // 点击左侧菜单项（增加商品发布的跳转逻辑）
    const selectMenuItem = (item) => {
      currentMenuItem.value = item;

      // 跳转到对应的管理页面
      if (item === '订单管理') {
        router.push('/merchant/ordersmanagemerchant');
      } else if (item === '商品管理') {
        router.push('/merchant/goodsmanage');
      } else if (item === '商品发布') { // 新增商品发布的跳转
        router.push({ name: 'PublishMerchant' });
      }
    };

    // 顶部导航按钮逻辑
    const goBack = () => {
      router.go(-1);
    };

    const goProfile = () => {
      if (!userId.value) {
        alert('请先登录');
        router.push({ name: 'Auth' });
        return;
      }
      router.push({ name: 'UserProfile' });
    };

    const logout = () => {
      if (confirm('确定要退出登录吗？')) {
        localStorage.removeItem('sessionId');
        localStorage.removeItem('userId');
        localStorage.removeItem('username');
        username.value = '';
        userId.value = '';
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
      showUserMenu,
      merchantSection,
      currentMenuItem,
      toggleSection,
      selectMenuItem,
      goBack,
      username,
      userId,
      userInitial,
      goProfile,
      logout,
      hideUserMenuTimeout,
      clearHideTimeout,
      // 新增：商品管理首页判断变量
      isGoodsManageHome
    };
  }
};
</script>

<style scoped>
/* 样式保持不变 */
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
}

.nav-buttons-container {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  min-width: 100px;
  padding: 0 15px;
  font-size: 14px;
  font-weight: bold;
  font-style: italic;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: #f5f5f5;
  white-space: nowrap;
}

.back-btn:hover {
  background-color: #e8e8e8;
  color: #8a2be2;
}

.user-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  font-weight: bold;
  font-style: italic;
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #8a2be2;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.user-menu-container {
  position: relative;
  display: inline-block;
}

.user-menu {
  position: absolute;
  right: 0;
  top: 100%;
  width: 150px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  z-index: 100;
  overflow: hidden;
}

.menu-item {
  display: block;
  width: 100%;
  padding: 10px 15px;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.menu-item:hover {
  background-color: #f0f0f0;
  color: #8a2be2;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.logo {
  color: #8a2be2;
  font-size: 20px;
  font-weight: bold;
}

.sidebar {
  width: 200px;
  padding: 20px;
  border-right: 1px solid #eee;
  background-color: #f5f5f5;
  height: calc(100vh - 65px);
  position: sticky;
  top: 65px;
}

.menu-section {
  margin-bottom: 15px;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  cursor: pointer;
}

.menu-header h3 {
  margin: 0;
  font-size: 16px;
}

.arrow {
  font-size: 12px;
}

.sidebar ul {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow: hidden;
}

.sidebar li {
  padding: 8px 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.sidebar li:hover {
  background-color: #e8e8e8;
  color: #8a2be2;
}

.sidebar li.active {
  background-color: #e8e8e8;
  color: #8a2be2;
  font-weight: bold;
}

.slide-enter-active, .slide-leave-active {
  transition: max-height 0.3s ease;
}

.slide-enter-from, .slide-leave-to {
  max-height: 0;
}

.slide-enter-to, .slide-leave-from {
  max-height: 200px;
}

.main-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  font-family: 'PingFang SC', sans-serif;
}

.content {
  display: flex;
  flex: 1;
}

.main-content {
  flex: 1;
  padding: 20px;
}
</style>