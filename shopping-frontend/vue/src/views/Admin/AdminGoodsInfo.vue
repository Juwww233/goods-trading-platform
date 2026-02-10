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

      <!-- 商品信息主区域 -->
      <div class="product-section">
        <!-- 商品图片区域 -->
        <div class="product-image">
          <img
              :src="product.img || 'https://picsum.photos/600/600?random=' + product.id"
              :alt="product.name"
          >
        </div>

        <!-- 商品信息区域 -->
        <div class="product-details">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-category">分类：{{ product.category }}</p>
          <p class="product-price">¥{{ product.price.toFixed(2) }}</p>
          <p class="product-address">发货地：{{ product.address || '未知' }}</p>
          <p class="product-date">上架时间：{{ formatDate(product.date) }}</p>

          <!-- 商品详情描述 -->
          <div class="product-description">
            <h3>商品详情</h3>
            <p>{{ product.content || '暂无详细描述' }}</p>
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
              <!-- 删除按钮 -->
              <button
                  class="delete-comment-btn"
                  @click="handleDeleteComment(item.id)"
              >
                删除
              </button>
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
import {
  getGoodsComments
} from '@/api/comment';
// 导入删除评论API
import { deleteComment } from '@/api/comment';

// 路由相关
const router = useRouter();
const route = useRoute();

// 状态管理
const loading = ref(true);
const error = ref('');
const product = ref({});
const comments = ref([]);
const loadingComments = ref(false);
const currentPage = ref(1);
const totalPages = ref(0);
const userId = ref(1); // 实际项目中应从登录状态获取

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '未知时间';
  const date = new Date(dateStr);
  return isNaN(date.getTime())
      ? dateStr
      : date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' });
};

// 加载商品详情
const loadProductDetail = async () => {
  loading.value = true;
  error.value = '';
  try {
    const productId = route.params.id;
    if (!productId || isNaN(Number(productId))) {
      error.value = '无效的商品ID';
      return;
    }

    const res = await getGoodsDetail(productId);
    if (res.success && res.data) {
      product.value = res.data;
      // 加载商品评论
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

// 加载评论
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

// 切换分页
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  loadComments(route.params.id);
};

// 处理删除评论
const handleDeleteComment = async (commentId) => {
  // 确认删除
  if (!confirm('确定要删除这条评论吗？')) {
    return;
  }

  try {
    const res = await deleteComment(commentId);
    if (res.success) {
      // 删除成功后重新加载评论列表
      loadComments(route.params.id);
      alert('评论删除成功');
    } else {
      alert(res.message || '删除评论失败');
    }
  } catch (err) {
    console.error('删除评论失败:', err);
    alert('网络异常，删除失败');
  }
};

// 页面初始化时加载数据
onMounted(() => {
  loadProductDetail();
});
</script>

<style scoped>
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
  transition: background-color 0.3s;
}

.error-container button:hover {
  background-color: #7b1fa2;
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
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.product-category {
  font-size: 16px;
  color: #666;
  margin-bottom: 15px;
}

.product-price {
  font-size: 28px;
  color: #e4393c;
  font-weight: bold;
  margin-bottom: 15px;
}

.product-address, .product-date {
  color: #666;
  margin-bottom: 10px;
  font-size: 14px;
}

/* 商品详情描述 */
.product-description {
  margin: 20px 0 30px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  line-height: 1.8;
}

.product-description h3 {
  margin-bottom: 10px;
  color: #333;
  font-size: 16px;
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
  align-items: center;
  margin-bottom: 8px;
  color: #888;
  font-size: 14px;
}

.comment-user {
  font-weight: bold;
}

/* 删除评论按钮样式 */
.delete-comment-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 12px;
  cursor: pointer;
  margin-left: 10px;
}

.delete-comment-btn:hover {
  background-color: #d93025;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .product-section {
    flex-direction: column;
  }

  .product-image {
    margin-right: 0;
    margin-bottom: 20px;
  }

  .product-name {
    font-size: 20px;
  }

  .product-price {
    font-size: 24px;
  }

  .comment-meta {
    flex-wrap: wrap;
  }
}
</style>