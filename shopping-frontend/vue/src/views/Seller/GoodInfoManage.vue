<template>
  <div class="good-info-container">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <p>加载中...</p>
    </div>

    <!-- 错误提示 -->
    <div v-else-if="error" class="error-container">
      <p>{{ error }}</p>
      <button @click="loadProductDetail">重试</button>
    </div>

    <!-- 商品详情（数据加载成功后显示） -->
    <div v-else>
      <!-- 保存按钮 -->
      <div class="header-section">
        <button
            class="save-button"
            @click="handleSaveChanges"
            :disabled="saving"
        >
          {{ saving ? '保存中...' : '保存修改' }}
        </button>
      </div>

      <!-- 保存成功提示 -->
      <div v-if="showSuccessToast" class="success-toast">
        商品信息更新成功！
      </div>

      <!-- 商品信息区域 -->
      <div class="product-section">
        <!-- 商品图片区域 -->
        <div class="product-image">
          <img
              :src="product.img || 'https://picsum.photos/600/600?random=' + product.id"
              :alt="product.name"
          >
        </div>

        <!-- 商品信息编辑区域 -->
        <div class="product-details">
          <!-- 商品名称（可编辑） -->
          <input
              v-model="product.name"
              class="product-name"
              placeholder="请输入商品名称"
          >

          <!-- 商品分类 -->
          <p class="product-category">分类：{{ product.category }}</p>

          <!-- 商品价格（可编辑） -->
          <input
              v-model.number="product.price"
              type="number"
              step="0.01"
              min="0.01"
              class="product-price"
              placeholder="请输入商品价格"
          >

          <p class="product-address">发货地：{{ product.address || '未知' }}</p>
          <p class="product-date">上架时间：{{ formatDate(product.date) }}</p>

          <!-- 销售状态（可编辑） -->
          <div class="sale-status-container">
            <label>销售状态：</label>
            <select
                v-model="product.saleStatus"
                class="sale-status-select"
            >
              <option value="已上架">已上架</option>
              <option value="未上架">未上架</option>
            </select>
          </div>

          <!-- 商品详情描述（可编辑） -->
          <div class="product-description">
            <h3>商品详情</h3>
            <textarea
                v-model="product.content"
                placeholder="请输入商品详情..."
                class="content-editor"
            ></textarea>
          </div>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section">
        <div class="comment-header">
          <h2 class="section-title">评论</h2>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div class="comment-item" v-for="item in comments" :key="item.id">
            <div class="comment-meta">
              <span class="comment-user">{{ item.userName || '用户' + item.userId }}</span>
              <span class="comment-time">{{ item.time }}</span>
            </div>
            <div class="comment-content">{{ item.content }}</div>
          </div>

          <!-- 无评论提示 -->
          <div v-if="comments.length === 0 && !loadingComments" class="no-comment">暂无评论</div>

          <!-- 分页控件 -->
          <div v-if="totalPages > 1" class="pagination">
            <button @click="changePage(currentPage - 1)" :disabled="currentPage <= 1">上一页</button>
            <span>{{ currentPage }} / {{ totalPages }}</span>
            <button @click="changePage(currentPage + 1)" :disabled="currentPage >= totalPages">下一页</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getGoodsDetail } from '@/api/goods';
import { updateGoods } from '@/api/goods';
import { getGoodsComments } from '@/api/comment';

// 路由相关
const router = useRouter();
const route = useRoute();

// 状态管理：移除userId定义
const loading = ref(true);
const error = ref('');
const product = ref({});
const saving = ref(false);
const showSuccessToast = ref(false);
// 评论相关状态
const comments = ref([]);
const loadingComments = ref(false);
const currentPage = ref(1);
const totalPages = ref(0);

// 加载商品详情（无需userId）
const loadProductDetail = async () => {
  loading.value = true;
  error.value = '';
  try {
    const productId = route.params.id;
    if (!productId || isNaN(Number(productId))) {
      error.value = '无效的商品ID';
      return;
    }

    // 加载商品详情时无需传递userId
    const res = await getGoodsDetail(productId, true);
    if (res.success && res.data) {
      product.value = res.data;
      loadComments(productId);
    } else {
      error.value = res.message || '获取商品详情失败';
    }
  } catch (err) {
    console.error('加载商品详情失败:', err);
    error.value = '网络错误，请检查连接后重试';
  } finally {
    loading.value = false;
  }
};

// 加载评论（无需userId）
const loadComments = async (productId) => {
  loadingComments.value = true;
  try {
    const res = await getGoodsComments(productId, currentPage.value);
    if (res.success && res.data) {
      comments.value = res.data.comments || [];
      totalPages.value = res.data.totalPages || 0;
    }
  } catch (err) {
    console.error('加载评论失败:', err);
  } finally {
    loadingComments.value = false;
  }
};

// 切换分页（无需userId）
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  loadComments(route.params.id);
};

// 保存修改：移除userId传递
const handleSaveChanges = async () => {
  // 简单验证
  if (!product.value.name || product.value.name.trim() === '') {
    alert('商品名称不能为空');
    return;
  }

  if (!product.value.price || product.value.price <= 0) {
    alert('请输入有效的商品价格');
    return;
  }

  saving.value = true;
  try {
    // 关键修改：不传递userId
    const productData = {
      ...product.value,
      // 移除userId字段
    };

    // 调用更新商品API（不包含userId）
    const res = await updateGoods(productData);

    if (res.success) {
      showSuccessToast.value = true;
      setTimeout(() => {
        showSuccessToast.value = false;
      }, 3000);
    } else {
      alert(res.message || '保存失败，请重试');
    }
  } catch (err) {
    console.error('保存商品信息失败:', err);
    alert('网络异常，保存失败');
  } finally {
    saving.value = false;
  }
};

// 格式化日期（保持不变）
const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间';
  const date = new Date(dateStr);
  return isNaN(date.getTime())
      ? dateStr
      : date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
};

// 页面初始化时加载数据
onMounted(() => {
  loadProductDetail();
});
</script>

<style scoped>
/* 样式保持不变 */
/* 基础容器样式 */
.good-info-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background-color: #fff;
  position: relative;
}

/* 加载和错误状态样式 */
.loading-container, .error-container {
  text-align: center;
  padding: 100px 0;
  color: #666;
}

.error-container button {
  margin-top: 15px;
  padding: 8px 16px;
  background-color: #8a2be2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* 头部区域 */
.header-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.save-button {
  background-color: #8a2be2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.save-button:hover:not(:disabled) {
  background-color: #7b1fa2;
}

.save-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

/* 商品信息区域 */
.product-section {
  display: flex;
  margin-bottom: 40px;
  border-bottom: 1px solid #eee;
  padding-bottom: 30px;
}

.product-image {
  flex: 1;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-right: 30px;
  background-color: #f9f9f9;
}

.product-image img {
  width: 100%;
  height: auto;
  object-fit: contain;
  max-height: 500px;
}

.product-details {
  flex: 1;
  padding: 10px;
}

.product-name {
  font-size: 24px;
  margin-bottom: 10px;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 10px;
  width: 100%;
}

.product-category {
  font-size: 16px;
  color: #666;
  margin-bottom: 15px;
}

.product-price {
  font-size: 24px;
  color: #e4393c;
  font-weight: bold;
  margin-bottom: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 8px 10px;
  width: 200px;
}

.product-address, .product-date {
  color: #666;
  margin-bottom: 10px;
  font-size: 14px;
}

.sale-status-container {
  margin: 20px 0;
  display: flex;
  align-items: center;
}

.sale-status-select {
  margin-left: 10px;
  padding: 6px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.product-description {
  margin: 20px 0 30px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.product-description h3 {
  margin-bottom: 10px;
  color: #333;
  font-size: 16px;
}

.content-editor {
  width: 100%;
  min-height: 200px;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  resize: vertical;
  line-height: 1.6;
}

/* 评论区样式 */
.comment-section {
  padding: 20px 0;
}

.section-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.comment-list {
  border-top: 1px solid #eee;
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
}

.comment-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #888;
  font-size: 14px;
}

.comment-user {
  font-weight: bold;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin: 20px 0;
}

.pagination button {
  padding: 5px 10px;
  border: 1px solid #ddd;
  background: white;
  cursor: pointer;
  border-radius: 4px;
}

.pagination button:disabled {
  color: #999;
  cursor: not-allowed;
}

.no-comment {
  text-align: center;
  padding: 30px;
  color: #999;
}

/* 成功提示 */
.success-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background-color: #4caf50;
  color: white;
  padding: 10px 20px;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  z-index: 1000;
  animation: fadein 0.5s, fadeout 0.5s 2.5s;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-section {
    flex-direction: column;
  }

  .product-image {
    margin-right: 0;
    margin-bottom: 20px;
  }

  .product-price {
    width: 100%;
  }
}

/* 动画效果 */
@keyframes fadein {
  from {top: 0; opacity: 0;}
  to {top: 20px; opacity: 1;}
}

@keyframes fadeout {
  from {top: 20px; opacity: 1;}
  to {top: 0; opacity: 0;}
}
</style>