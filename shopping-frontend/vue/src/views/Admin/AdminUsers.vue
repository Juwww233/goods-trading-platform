<template>
  <div class="admin-users">
    <div class="page-header">
      <div class="header-content">
        <router-link to="/admin" class="back-btn">
          <i class="fa fa-arrow-left mr-2"></i>返回
        </router-link>
        <h2 class="page-title">用户管理</h2>
      </div>
      <div class="search-bar">
        <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索用户名/ID/角色"
            @keyup.enter="fetchUsers"
        />
        <button @click="fetchUsers">搜索</button>
      </div>
    </div>

    <div class="user-table">
      <table>
        <thead>
        <tr>
          <th>用户ID</th>
          <th>用户名</th>
          <th>手机号</th>
          <th>角色</th> <!-- 添加角色列 -->
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="user in userList" :key="user.id">
          <td>{{ user.id }}</td>
          <td>{{ user.username || user.name || '无姓名' }}</td>
          <td>{{ user.phone }}</td>
          <td>
              <span class="role-badge"
              :class="user.role === 'buyer' ? 'buyer' :
                 user.role === 'seller' ? 'seller' :
                 user.role === 'admin' ? 'admin' : 'unknown'">
                  {{ user.role === 'buyer' ? '买家' :
                    user.role === 'seller' ? '卖家' :
                    user.role === 'admin' ? '管理员' : '未知角色' }}
              </span>
          </td>
          <td>
            <button class="delete-btn" @click="handleDelete(user.id)">删除</button>
          </td>
        </tr>
        </tbody>
      </table>

      <div class="pagination">
        <button @click="handlePrevPage" :disabled="currentPage === 1">上一页</button>
        <span>第 {{ currentPage }} 页</span>
        <button @click="handleNextPage" :disabled="currentPage >= totalPages">下一页</button>
        <select v-model="pageSize" @change="fetchUsers">
          <option value="10">10 条/页</option>
          <option value="20">20 条/页</option>
          <option value="50">50 条/页</option>
        </select>
        <span>共 {{ total }} 条数据</span>
      </div>
    </div>

    <div v-if="showConfirm" class="confirm-modal">
      <div class="confirm-content">
        <p class="confirm-message">{{ confirmMessage }}</p>
        <div class="confirm-buttons">
          <button class="cancel-btn" @click="cancelDelete">取消</button>
          <button class="confirm-btn" @click="confirmDelete">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { getUsers, deleteUser as deleteUserApi } from '@/api/admin';

const userList = ref([]);
const searchKeyword = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const currentUserId = ref(null);
const showConfirm = ref(false);
const confirmMessage = ref('确定要删除该用户吗？此操作不可撤销。');

const totalPages = computed(() => Math.ceil(total.value / pageSize.value));

const fetchUsers = async () => {
  try {
    const res = await getUsers({
      keyword: searchKeyword.value,
      page: currentPage.value,
      pageSize: pageSize.value
    });
    console.log('用户列表响应:', res);
    if (res.success) {
      userList.value = res.data.list || [];
      total.value = res.data.total || 0;
    } else {
      alert(res.message || '获取用户列表失败');
    }
  } catch (error) {
    console.error('获取用户列表错误:', error);
    alert('网络错误，请重试');
  }
};

const handleDelete = (userId) => {
  currentUserId.value = userId;
  showConfirm.value = true;
};

const confirmDelete = async () => {
  if (!currentUserId.value) return;

  try {
    const res = await deleteUserApi(currentUserId.value);
    if (res.success) {
      alert('删除成功');
      fetchUsers();
    } else {
      alert(res.message || '删除失败');
    }
  } catch (error) {
    console.error('删除用户错误:', error);
    alert('网络错误，请重试');
  } finally {
    showConfirm.value = false;
    currentUserId.value = null;
  }
};

const cancelDelete = () => {
  showConfirm.value = false;
  currentUserId.value = null;
};

const handlePrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    fetchUsers();
  }
};

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    fetchUsers();
  }
};

const editUser = (user) => {
  console.log('编辑用户:', user);
  // 实现编辑逻辑
};

onMounted(() => {
  fetchUsers();
});
</script>

<style scoped>
/* 全局容器样式 */
.admin-users {
  padding: 20px;
  max-width: 1200px; /* 限制最大宽度，让内容居中且不过于宽泛 */
  margin: 0 auto; /* 水平居中 */
}

/* 头部区域样式 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-content {
  display: flex;
  align-items: center;
}
.back-btn {
  display: inline-flex;
  align-items: center;
  color: #409EFF;
  text-decoration: none;
  cursor: pointer;
  transition: color 0.3s ease;
}
.back-btn:hover {
  color: #66b1ff;
}
.page-title {
  margin-left: 10px;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

/* 搜索栏样式 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}
.search-bar input {
  width: 250px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  outline: none;
  transition: border-color 0.3s ease;
}
.search-bar input:focus {
  border-color: #409EFF;
}
.search-bar button {
  padding: 8px 15px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}
.search-bar button:hover {
  background-color: #66b1ff;
}

/* 用户表格区域样式 */
.user-table {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05); /* 柔和阴影 */
}
table {
  width: 100%;
  border-collapse: collapse;
}
th,
td {
  border: 1px solid #eee; /* 更浅的边框色 */
  padding: 10px 8px; /* 增加内边距，让内容不拥挤 */
  text-align: left;
}
th {
  background-color: #f8f9fa;
  font-weight: bold;
  color: #666;
}
tr {
  transition: background-color 0.3s ease;
}
tr:hover {
  background-color: #fafafa; /*  hover 效果更柔和 */
}

/* 操作按钮样式 */
.edit-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  margin-right: 10px;
}
.edit-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
}
.delete-btn {
  background-color: #f56c6c;
  color: white;
  border: none;
  padding: 6px 12px; /* 调整内边距 */
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}
.delete-btn:hover {
  background-color: #f78989;
}

/* 分页区域样式 */
.pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}
.pagination button {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background-color: #fff;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s ease;
}
.pagination button:hover {
  border-color: #409EFF;
  color: #409EFF;
}
.pagination button:disabled {
  cursor: not-allowed;
  background-color: #f8f9fa;
  color: #999;
  border-color: #ddd;
}
.pagination select {
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  outline: none;
  transition: border-color 0.3s ease;
}
.pagination select:focus {
  border-color: #409EFF;
}

/* 确认对话框样式 */
.confirm-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.confirm-content {
  background-color: white;
  padding: 24px; /* 增加内边距 */
  border-radius: 8px;
  width: 320px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1); /* 更明显的阴影 */
}
.confirm-message {
  margin-bottom: 20px;
  color: #333;
}
.confirm-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}
.confirm-btn,
.cancel-btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}
.confirm-btn {
  background-color: #f56c6c;
  color: white;
}
.confirm-btn:hover {
  background-color: #f78989;
}
.cancel-btn {
  background-color: #ddd;
  color: #333;
}
.cancel-btn:hover {
  background-color: #ccc;
}
.role-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.buyer {
  background-color: #e6f7ff;
  color: #1890ff;
}

.seller {
  background-color: #fff2e8;
  color: #fa8c16;
}
</style>