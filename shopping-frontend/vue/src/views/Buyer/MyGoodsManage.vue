<template>
  <div class="category-page">
    <!-- 页面标题 -->
    <h2 class="category-title">我的商品</h2>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <p>加载中...</p>
    </div>

    <!-- 商品列表 -->
    <div v-else class="product-grid">
      <div
          v-if="products.length > 0"
          class="product-card"
          v-for="product in products"
          :key="product.id"
          @click="goToProductDetail(product.id)"
      >
        <div class="product-image">
          <img
              :src="product.img || 'https://picsum.photos/300/300?random=' + product.id"
              :alt="product.name"
              class="img-cover"
          >
        </div>
        <div class="product-info">
          <h3 class="product-name">{{ product.name }}</h3>
          <p class="product-desc">{{ product.content || '暂无描述' }}</p>
          <div class="product-footer">
            <span class="product-price">¥{{ product.price.toFixed(2) }}</span>
            <div class="product-actions">
              <button class="delete-btn" @click.stop="deleteProduct(product.id)">删除</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 无结果提示 -->
      <div v-else class="no-results">
        <p>暂无商品</p>
        <button class="add-btn" @click="addNewProduct">添加新商品</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getGoodsByUserId, deleteGoods } from '@/api/goods';

const router = useRouter();
const loading = ref(false);
const products = ref([]);
const userId = ref(null); // 从localStorage读取用户ID

// 加载用户ID
const loadUserId = () => {
  const storedUserId = localStorage.getItem('userId');
  if (storedUserId) {
    userId.value = storedUserId;
  } else {
    // 未登录时跳转登录页
    alert('请先登录查看您的商品');
    router.push({ name: 'Auth' });
  }
};

// 加载商品数据
const loadProducts = async () => {
  if (!userId.value) return; // 未获取到userId则不请求

  loading.value = true;
  try {
    const res = await getGoodsByUserId(userId.value);
    products.value = res.data || [];
  } catch (error) {
    console.error(`获取用户ID=${userId.value}的商品失败:`, error);
    alert('加载商品失败，请刷新重试');
  } finally {
    loading.value = false;
  }
};

// 初始化加载
onMounted(() => {
  loadUserId();
  loadProducts();
});

// 跳转到商品详情页 - 修改为指定的路由配置
const goToProductDetail = (productId) => {
  router.push({
    name: 'GoodInfoManage',  // 匹配路由配置中的name
    params: { id: productId } // 传递商品ID参数
  });
};

// 删除商品
const deleteProduct = async (productId) => {
  if (confirm('确定要删除该商品吗？')) {
    try {
      const res = await deleteGoods(productId);
      if (res.success) {
        alert('删除成功');
        loadProducts(); // 重新加载商品列表
      } else {
        alert('删除失败: ' + res.message);
      }
    } catch (error) {
      console.error('删除商品失败:', error);
      alert('网络异常，删除失败');
    }
  }
};

// 添加新商品
const addNewProduct = () => {
  router.push({ name: 'Publish' });
};
</script>

<style scoped>
/* 样式保持不变 */
.category-page {
  padding: 20px;
}

.category-title {
  color: #333;
  font-size: 24px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.loading-container {
  text-align: center;
  padding: 80px 0;
  color: #666;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
}

.product-card:hover {
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  transform: translateY(-3px);
}

.product-image {
  height: 200px;
  background-color: #f5f5f5;
  position: relative;
  overflow: hidden;
}

.img-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-name {
  color: #333;
  font-size: 18px;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 15px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  color: #8a2be2;
  font-weight: bold;
  font-size: 18px;
}

.product-actions {
  display: flex;
  gap: 10px;
}

.delete-btn {
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.delete-btn:hover {
  background-color: #d32f2f;
}

.add-btn {
  margin-top: 20px;
  padding: 8px 16px;
  background-color: #8a2be2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:hover {
  background-color: #7b1fa2;
}

.no-results {
  text-align: center;
  padding: 80px 0;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
