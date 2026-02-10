<template>
  <div class="category-page">
    <!-- 分类标题 -->
    <h2 class="category-title">{{ categoryTitle }}</h2>

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
              <!-- <button class="buy-btn" @click.stop="goToOrder(product.id)">立即购买</button>-->
            </div>
          </div>
        </div>
      </div>

      <!-- 无结果提示 -->
      <div v-else class="no-results">
        <p>暂无商品</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getGoodsByCategory } from '@/api/goods';

const router = useRouter();
const loading = ref(false);
const products = ref([]);
const categoryTitle = ref('电子产品');

// 加载商品数据
const loadProducts = async () => {
  const category = router.currentRoute.value.params.type || '电子产品';
  console.log('当前分类:', category);
  categoryTitle.value = category;
  loading.value = true;

  try {
    const res = await getGoodsByCategory(category);
    console.log('接口响应:', res);
    products.value = res.data || [];
    console.log('商品列表:', products.value);
  } catch (error) {
    console.error(`获取${category}商品失败:`, error);
    alert('加载商品失败，请刷新重试');
  } finally {
    loading.value = false;
  }
};

// 初始化和监听路由变化
onMounted(loadProducts);
watch(() => router.currentRoute.value.params.type, loadProducts);

// 跳转到商品详情页（修正路由名称为GoodInfo）
const goToProductDetail = (productId) => {
  router.push({
    name: 'GoodInfo', // 改为与路由配置一致的名称
    params: { id: productId }
  });
};

// 跳转到订单页
const goToOrder = (productId) => {
  router.push({
    name: 'Order',
    query: { productId }
  });
};
</script>

<style scoped>
/* 样式部分保持不变 */
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

.buy-btn {
  background-color: #ff6600;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.buy-btn:hover {
  background-color: #e55c00;
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