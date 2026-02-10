<template>
  <div class="admin-dashboard">
    <div class="header">
      <h1>管理员中心</h1>
      <div class="user-controls">
        <div class="welcome-message">
          欢迎，<span>{{ adminName }}</span>
        </div>
        <button class="logout-btn" @click="logout">
          <i class="fa fa-sign-out"></i> 退出登录
        </button>
      </div>
    </div>

    <div class="stats-cards">
      <div class="card" @click="navigateTo('AdminUsers')">
        <div class="card-icon">
          <i class="fa fa-users"></i>
        </div>
        <div class="card-content">
          <h3>用户管理</h3>
          <p>管理系统用户信息</p>
        </div>
      </div>

      <div class="card" @click="navigateTo('AdminGoods')">
        <div class="card-icon">
          <i class="fa fa-shopping-bag"></i>
        </div>
        <div class="card-content">
          <h3>商品管理</h3>
          <p>审核和管理商品</p>
        </div>
      </div>

      <div class="card" @click="navigateTo('AdminAnnouncements')">
        <div class="card-icon">
          <i class="fa fa-bullhorn"></i>
        </div>
        <div class="card-content">
          <h3>公告管理</h3>
          <p>发布和管理系统公告</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getAdminInfo } from '@/api/admin';
//import { ElMessage } from 'element-plus';

const router = useRouter();
const adminName = ref('');

// 获取管理员信息
const fetchAdminInfo = async () => {
  try {
    const res = await getAdminInfo();
    if (res.success) {
      adminName.value = res.data.name || res.data.username;
    }
  } catch (error) {
    console.error('获取管理员信息失败:', error);
  }
};

// 导航到子页面
const navigateTo = (routeName) => {
  router.push({ name: routeName });
};

// 退出登录
const logout = () => {
  localStorage.removeItem('sessionId');
  localStorage.removeItem('userId');
  localStorage.removeItem('role');
  confirm('已成功退出');
  router.push({ name: 'Auth' });
};

// 页面初始化
onMounted(() => {
  // 验证管理员权限
  const role = localStorage.getItem('role');
  if (role !== 'admin') {
    confirm('没有管理员权限');
    router.push({ name: 'Home' });
    return;
  }

  // 获取管理员信息
  fetchAdminInfo();
});
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.user-controls {
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-message {
  font-size: 16px;
  color: #666;
}

.welcome-message span {
  font-weight: bold;
  color: #333;
}

.logout-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 15px;
  cursor: pointer;
  transition: background-color 0.3s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.logout-btn:hover {
  background-color: #f78989;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  margin-top: 50px;
}

.card {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.08);
  padding: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #eee;
}

.card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 30px rgba(0,0,0,0.12);
  border-color: #409eff;
}

.card-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 20px;
}

.card-content {
  text-align: center;
}

.card-content h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
  color: #333;
}

.card-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>