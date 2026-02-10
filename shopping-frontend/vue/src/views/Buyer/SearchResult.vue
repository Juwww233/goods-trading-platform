<template>
  <div class="search-result-page">
    <h2 class="result-title">
      搜索结果: "{{ keyword }}"
    </h2>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <p>搜索中...</p>
    </div>

    <!-- 商品列表 -->
    <div v-else class="product-grid">
      <div
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
          <p class="product-price">¥{{ product.price.toFixed(2) }}</p>
          <p class="product-desc">{{ product.content || '暂无描述' }}</p>
        </div>
      </div>

      <!-- 无结果提示 -->
      <div v-if="products.length === 0 && !loading" class="no-results">
        <p>未找到相关商品</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { searchGoods } from '@/api/goods'; // 引入搜索API

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const products = ref([]);
const keyword = ref('');

// 加载搜索结果
const loadSearchResults = async () => {
  loading.value = true;
  const newKeyword = route.query.keyword || ''; // 从路由参数获取最新关键词
  keyword.value = newKeyword;

  try {
    if (!newKeyword.trim()) {
      products.value = [];
      return;
    }

    // 调用后端搜索接口
    const res = await searchGoods(newKeyword);
    products.value = res.data || [];
  } catch (error) {
    console.error('搜索商品失败:', error);
    alert('搜索失败，请重试');
  } finally {
    loading.value = false;
  }
};

// 初始化加载
onMounted(loadSearchResults);

// 关键修复：监听路由参数变化，当keyword改变时重新加载数据
watch(
    () => route.query.keyword, // 监听路由中的keyword参数
    (newKeyword, oldKeyword) => {
      if (newKeyword !== oldKeyword) { // 只有当关键词真正变化时才重新加载
        loadSearchResults();
      }
    }
);

// 跳转到商品详情页
const goToProductDetail = (productId) => {
  router.push({
    name: 'GoodInfo',
    params: { id: productId }
  });
};
</script>

<style scoped>
/* 保持原有样式不变 */
.search-result-page {
  padding: 20px;
}

.result-title {
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.loading-container {
  text-align: center;
  padding: 50px 0;
  color: #666;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.product-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  transform: translateY(-3px);
}

.product-image {
  height: 200px;
  background-color: #f9f9f9;
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
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  color: #e4393c;
  font-weight: bold;
  margin-bottom: 5px;
}

.product-desc {
  color: #666;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.no-results {
  text-align: center;
  padding: 80px 0;
  color: #666;
}
</style>
