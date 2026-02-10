<template>
  <div class="admin-announcements">
    <!-- 顶部标题栏：实现返回左、标题中、发布按钮右 -->
    <div class="page-header">
      <button class="btn back" @click="handleBack">返回</button>
      <h2 class="title">公告管理</h2>
      <button class="btn primary" @click="openCreateDialog">发布公告</button>
    </div>

    <!-- 发布/编辑公告弹窗 -->
    <div class="dialog-mask" v-if="dialogVisible">
      <div class="dialog">
        <div class="dialog-header">
          <h3>{{ dialogTitle }}</h3>
          <span class="close" @click="dialogVisible = false">&times;</span>
        </div>
        <div class="dialog-body">
          <form class="form">
            <div class="form-item">
              <label>标题：</label>
              <input
                  type="text"
                  v-model="form.title"
                  placeholder="请输入公告标题"
                  class="input"
              />
            </div>
            <div class="form-item">
              <label>内容：</label>
              <textarea
                  v-model="form.content"
                  placeholder="请输入公告内容"
                  rows="6"
                  class="textarea"
              ></textarea>
            </div>
          </form>
        </div>
        <div class="dialog-footer">
          <button class="btn" @click="dialogVisible = false">取消</button>
          <button class="btn primary" @click="saveAnnouncement">
            {{ form.id ? '确认编辑' : '确认发布' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 公告列表 -->
    <div class="announcement-table">
      <!-- 加载状态 -->
      <div class="loading" v-if="loading">加载中...</div>
      <!-- 空状态 -->
      <div class="empty" v-else-if="announcementList.length === 0">
        暂无公告数据
      </div>
      <!-- 列表展示 -->
      <table v-else>
        <thead>
        <tr>
          <th>公告ID</th>
          <th>标题</th>
          <th>发布者</th>
          <th>发布时间</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr
            v-for="(item, index) in announcementList"
            :key="index"
        >
          <td>{{ item.id }}</td>
          <td>{{ item.title }}</td>
          <td>{{ item.user }}</td>
          <td>{{ formatTime(item.time) }}</td>
          <td>
            <button class="btn edit" @click="openEditDialog(item)">编辑</button>
            <button class="btn delete" @click="handleDelete(item.id)">删除</button>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <button
            class="btn"
            :disabled="currentPage === 1"
            @click="handlePrevPage"
        >上一页</button>
        <span>第 {{ currentPage }} 页 / 共 {{ totalPage }} 页</span>
        <button
            class="btn"
            :disabled="currentPage === totalPage"
            @click="handleNextPage"
        >下一页</button>
        <select
            v-model="pageSize"
            @change="fetchAnnouncements"
        >
          <option value="10">10条/页</option>
          <option value="20">20条/页</option>
          <option value="50">50条/页</option>
        </select>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
// 假设接口请求模块，需根据实际项目路径调整
import {
  getAnnouncements,
  createAnnouncement,
  deleteAnnouncement,
  updateAnnouncement
} from '@/api/admin';
import dayjs from 'dayjs';

// 响应式数据
const announcementList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const dialogVisible = ref(false);
const form = ref({
  id: '',
  title: '',
  content: '',
  user: '系统管理员',
  time: dayjs().format('YYYY-MM-DD HH:mm:ss')
});
const dialogTitle = ref('发布公告');
const loading = ref(false);

// 获取公告列表
const fetchAnnouncements = async () => {
  loading.value = true;
  try {
    const res = await getAnnouncements({
      page: currentPage.value,
      pageSize: pageSize.value
    });
    if (res && res.success && res.data) {
      announcementList.value = res.data.list || [];
      total.value = res.data.total || 0;
    } else {
      alert(res.message || '获取公告列表失败');
      announcementList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取公告列表错误:', error);
    alert('网络错误，请重试');
    announcementList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
};

// 计算总页数
const totalPage = computed(() => {
  return Math.ceil(total.value / pageSize.value);
});

// 分页切换
const handlePrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    fetchAnnouncements();
  }
};
const handleNextPage = () => {
  if (currentPage.value < totalPage.value) {
    currentPage.value++;
    fetchAnnouncements();
  }
};

// 打开发布弹窗
const openCreateDialog = () => {
  form.value = {
    id: '',
    title: '',
    content: '',
    user: '系统管理员',
    time: dayjs().format('YYYY-MM-DD HH:mm:ss')
  };
  dialogTitle.value = '发布公告';
  dialogVisible.value = true;
};

// 打开编辑弹窗
const openEditDialog = (row) => {
  if (row && row.id) {
    form.value = { ...row };
    dialogTitle.value = '编辑公告';
    dialogVisible.value = true;
  } else {
    alert('无效的公告数据');
  }
};

// 保存公告（新增/编辑）
const saveAnnouncement = async () => {
  try {
    let res;
    if (!form.value.id) {
      res = await createAnnouncement(form.value);
    } else {
      res = await updateAnnouncement(form.value);
    }
    if (res && res.success) {
      alert(form.value.id ? '编辑成功' : '发布成功');
      dialogVisible.value = false;
      fetchAnnouncements();
    } else {
      alert(res.message || '操作失败');
    }
  } catch (error) {
    console.error('保存公告错误:', error);
    alert('网络错误，请重试');
  }
};

// 删除公告
const handleDelete = async (id) => {
  if (confirm('确定要删除此公告吗？删除后无法恢复')) {
    try {
      const res = await deleteAnnouncement(id);
      if (res && res.success) {
        alert('删除成功');
        fetchAnnouncements();
      } else {
        alert(res.message || '删除失败');
      }
    } catch (error) {
      console.error('删除公告错误:', error);
      alert('网络错误，请重试');
    }
  }
};

// 时间格式化
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 返回按钮逻辑
const handleBack = () => {
  window.history.back();
};

// 初始化加载数据
onMounted(() => {
  fetchAnnouncements();
});
</script>

<style scoped>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.admin-announcements {
  padding: 20px;
  font-family: Arial, sans-serif;
}

/* 顶部标题栏：实现返回左、标题中、发布按钮右 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

/* 按钮通用样式 */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
}
.btn:hover {
  opacity: 0.8;
}
.back {
  background-color: #666;
  color: #fff;
}
.primary {
  background-color: #409eff;
  color: #fff;
}
.edit {
  background-color: #67c23a;
  color: #fff;
  margin-right: 8px;
}
.delete {
  background-color: #f56c6c;
  color: #fff;
}

/* 标题样式，让其在中间位置（配合 justify-content: space-between ，按钮分别在两端，标题自然居中） */
.title {
  font-size: 20px;
  font-weight: bold;
}

/* 弹窗遮罩层 */
.dialog-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}
.dialog {
  width: 500px;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  animation: fadeIn 0.3s ease;
}
.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}
.dialog-header h3 {
  font-size: 16px;
}
.close {
  font-size: 20px;
  cursor: pointer;
  color: #909399;
}
.dialog-body {
  padding: 20px;
}
.form-item {
  margin-bottom: 16px;
}
.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
}
.input, .textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #dcdcdc;
  border-radius: 4px;
  resize: none;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px;
  background: #f5f7fa;
  border-top: 1px solid #ebeef5;
}

/* 公告列表 */
.announcement-table {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  padding: 12px 8px;
  border-bottom: 1px solid #ebeef5;
  text-align: left;
}
th {
  background: #f5f7fa;
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}
.pagination select {
  padding: 6px;
  border-radius: 4px;
  border: 1px solid #dcdcdc;
}

/* 动画 */
@keyframes fadeIn {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>