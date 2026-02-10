<template>
  <div class="admin-goods">
    <!-- 页面头部：标题、返回键和筛选区 -->
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        ← 返回
      </button>
      <h2>商品管理</h2>
      <div class="filter-bar">
        <!-- 状态筛选下拉框 -->
        <select
            v-model="statusFilter"
            @change="fetchGoods"
            class="status-select"
        >
          <option value="">全部</option>
          <option value="未审核">待审核</option>
          <option value="审核通过">已通过</option>
          <option value="审核未通过">已拒绝</option>
        </select>

        <!-- 搜索框 -->
        <input
            type="text"
            v-model="searchKeyword"
            placeholder="搜索商品名称/ID"
            @keyup.enter="fetchGoods"
            class="search-input"
        >
        <button @click="fetchGoods" class="search-btn">搜索</button>
      </div>
    </div>

    <!-- 商品表格 -->
    <div class="goods-table">
      <table class="data-table">
        <thead>
        <tr>
          <th>商品ID</th>
          <th>商品图片</th>
          <th>商品名称</th>
          <th>价格</th>
          <th>卖家</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="goods in goodsList" :key="goods.id" class="table-row">
          <td>{{ goods.id }}</td>
          <td>
            <img
                :src="goods.img"
                alt="商品图片"
                class="goods-img"
                @error="handleImageError($event, goods)"
            />
          </td>
          <td class="goods-name">{{ goods.name }}</td>
          <td>¥{{ goods.price.toFixed(2) }}</td>
          <td>{{ goods.sellerName || '未知卖家' }}</td>
          <td>
              <span
                  class="status-tag"
                  :class="getStatusClass(goods.status)"
              >
                {{ getStatusText(goods.status) }}
              </span>
          </td>
          <td class="operation-buttons">
            <!-- 只对已通过的商品显示查看按钮 -->
            <button
                class="btn view-btn"
                @click="viewGoods(goods.id)"
                v-if="goods.status === '审核通过'"
            >
              查看
            </button>

            <button
                class="btn approve-btn"
                v-if="goods.status === '未审核'"
                @click="handleApproveGoods(goods.id)"
            >
              通过
            </button>

            <button
                class="btn reject-btn"
                v-if="goods.status === '未审核'"
                @click="showRejectModal(goods.id)"
            >
              拒绝
            </button>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 无数据提示 -->
      <div class="empty-state" v-if="goodsList.length === 0 && total === 0">
        暂无商品数据
      </div>

      <!-- 分页控件 -->
      <div class="pagination" v-if="total > 0">
        <button
            class="page-btn"
            @click="handlePrevPage"
            :disabled="currentPage === 1"
        >
          上一页
        </button>

        <div class="page-info">
          第 {{ currentPage }} / {{ totalPages }} 页
        </div>

        <button
            class="page-btn"
            @click="handleNextPage"
            :disabled="currentPage >= totalPages"
        >
          下一页
        </button>

        <div class="page-size">
          <span>每页显示:</span>
          <select
              v-model="pageSize"
              @change="fetchGoods"
              class="size-select"
          >
            <option value="10">10</option>
            <option value="20">20</option>
            <option value="50">50</option>
          </select>
          <span>条，共 {{ total }} 条</span>
        </div>
      </div>
    </div>

    <!-- 拒绝原因弹窗 -->
    <div class="modal-backdrop" v-if="showRejectModalVisible">
      <div class="modal-content">
        <h3 class="modal-title">拒绝商品</h3>
        <p class="modal-desc">请输入拒绝原因</p>
        <textarea
            v-model="rejectReason"
            placeholder="请输入拒绝原因"
            class="reason-input"
        ></textarea>
        <div class="modal-buttons">
          <button
              class="modal-btn cancel-btn"
              @click="showRejectModalVisible = false"
          >
            取消
          </button>
          <button
              class="modal-btn confirm-btn"
              @click="confirmRejectGoods"
          >
            确定
          </button>
        </div>
      </div>
    </div>

    <!-- 消息提示 -->
    <div class="toast" v-if="toastMessage">
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { getGoods, approveGoods, rejectGoods } from '@/api/admin';

const router = useRouter();

// 商品列表数据
const goodsList = ref([]);
const searchKeyword = ref('');
const statusFilter = ref('');
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 拒绝商品相关
const showRejectModalVisible = ref(false);
const currentGoodsId = ref(null);
const rejectReason = ref('');

// 消息提示
const toastMessage = ref('');

// 总页数计算
const totalPages = computed(() => Math.ceil(total.value / pageSize.value));

// 返回上一页
const goBack = () => {
  router.push({ name: 'Admin' });
};

// 获取商品列表
const fetchGoods = async () => {
  try {
    const res = await getGoods({
      keyword: searchKeyword.value,
      status: statusFilter.value,
      page: currentPage.value,
      pageSize: pageSize.value
    });

    if (res.success) {
      goodsList.value = res.data.list;
      total.value = res.data.total || 0;
    } else {
      showToast(res.message || '获取商品列表失败');
    }
  } catch (error) {
    console.error('获取商品错误:', error);
    showToast('网络错误，请重试');
  }
};

// 显示消息提示
const showToast = (message, duration = 3000) => {
  toastMessage.value = message;
  setTimeout(() => {
    toastMessage.value = '';
  }, duration);
};

// 分页处理
const handlePrevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--;
    fetchGoods();
  }
};

const handleNextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++;
    fetchGoods();
  }
};

// 状态文本转换
const getStatusText = (status) => {
  switch (status) {
    case '未审核': return '待审核';
    case '审核通过': return '已通过';
    case '审核未通过': return '已拒绝';
    default: return '未知状态';
  }
};

// 状态样式类名
const getStatusClass = (status) => {
  switch (status) {
    case '未审核': return 'pending';
    case '审核通过': return 'approved';
    case '审核未通过': return 'rejected';
    default: return 'unknown';
  }
};

const handleImageError = (event, goods) => {
  event.target.src = 'https://picsum.photos/200/300?random=1';
  event.target.onerror = null;
};

// 审核通过
const handleApproveGoods = async (goodsId) => {
  try {
    const res = await approveGoods(goodsId);
    if (res.success) {
      showToast('审核通过成功');
      fetchGoods();
    } else {
      showToast(res.message || '操作失败');
    }
  } catch (error) {
    console.error('审核通过错误:', error);
    showToast('网络错误，请重试');
  }
};

// 显示拒绝弹窗
const showRejectModal = (goodsId) => {
  currentGoodsId.value = goodsId;
  rejectReason.value = '';
  showRejectModalVisible.value = true;
};

// 确认拒绝商品
const confirmRejectGoods = async () => {
  if (!rejectReason.value.trim()) {
    showToast('拒绝原因不能为空');
    return;
  }

  try {
    const res = await rejectGoods(currentGoodsId.value, rejectReason.value);
    if (res.success) {
      showToast('已拒绝该商品');
      showRejectModalVisible.value = false;
      fetchGoods();
    } else {
      showToast(res.message || '操作失败');
    }
  } catch (error) {
    console.error('拒绝商品错误:', error);
    showToast('网络错误，请重试');
  }
};

// 查看商品详情
const viewGoods = (goodsId) => {
  router.push({
    name: 'AdminGoodsInfo',
    params: { id: goodsId }
  });
};

// 页面初始化
onMounted(() => {
  fetchGoods();
});
</script>

<style scoped>
/* 样式保持不变 */
.back-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  margin-right: 10px;
}
.back-btn:hover {
  border-color: #409EFF;
  color: #409EFF;
}
.admin-goods {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.filter-bar {
  display: flex;
  gap: 10px;
  align-items: center;
}

.status-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #fff;
  cursor: pointer;
}

.search-input {
  width: 250px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.search-btn {
  padding: 8px 15px;
  background-color: #409EFF;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.search-btn:hover {
  background-color: #66b1ff;
}

.goods-table {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th:nth-child(6),
.data-table th:nth-child(7) {
  vertical-align: middle;
  width: 150px;
}

.data-table td:nth-child(6),
.data-table td:nth-child(7) {
  vertical-align: middle;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
  color: #666;
  font-size: 14px;
}

.table-row:hover {
  background-color: #fafafa;
}

.goods-img {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.goods-name {
  max-width: 250px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-tag {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.pending {
  background-color: #fff3cd;
  color: #856404;
}

.status-tag.approved {
  background-color: #d4edda;
  color: #155724;
}

.status-tag.rejected {
  background-color: #f8d7da;
  color: #721c24;
}

.operation-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  height: 100%;
}

.btn {
  padding: 5px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.2s;
}

.view-btn {
  background-color: #e6f7ff;
  color: #1890ff;
}

.view-btn:hover {
  background-color: #bae7ff;
}

.approve-btn {
  background-color: #f0f9eb;
  color: #52c41a;
}

.approve-btn:hover {
  background-color: #c3e6be;
}

.reject-btn {
  background-color: #fff1f0;
  color: #f5222d;
}

.reject-btn:hover {
  background-color: #ffccc7;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #999;
  border-top: 1px solid #eee;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  padding: 15px;
  border-top: 1px solid #eee;
  margin-top: 10px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #409EFF;
  color: #409EFF;
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  color: #999;
}

.page-info {
  color: #666;
  font-size: 14px;
}

.page-size {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.size-select {
  padding: 4px 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.modal-backdrop {
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

.modal-content {
  width: 400px;
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-title {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #333;
}

.modal-desc {
  margin: 0 0 15px 0;
  color: #666;
  font-size: 14px;
}

.reason-input {
  width: 100%;
  height: 100px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  margin-bottom: 15px;
}

.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-btn {
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  border: none;
  font-size: 14px;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #333;
}

.confirm-btn {
  background-color: #f5222d;
  color: white;
}

.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 20px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  border-radius: 4px;
  z-index: 2000;
  animation: fadeInOut 3s;
}

@keyframes fadeInOut {
  0% { opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { opacity: 0; }
}

@media (max-width: 768px) {
  .filter-bar {
    flex-wrap: wrap;
  }

  .search-input {
    width: 100%;
  }

  .modal-content {
    width: 90%;
  }

  .goods-name {
    max-width: 100px;
  }
}
</style>