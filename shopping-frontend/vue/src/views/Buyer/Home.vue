<template>
  <div class="home-page">
    <!-- 分类标签 -->
    <div class="category-tags">
      <span
          v-for="(category, index) in categories"
          :key="index"
          :class="{ 'tag': true, 'active': activeCategory === category }"
          @click="changeCategory(category)"
      >
        {{ category }}
      </span>
    </div>

    <!-- 猜你喜欢 -->
    <div class="section">
      <h2 class="section-title">猜你喜欢</h2>
      <div class="product-grid">
        <div
            v-for="(goods, index) in guessYouLikeList"
            :key="goods.id || index"
            class="product-card"
            @click="goToGoodDetail(goods.id)"
        >
          <div class="product-img">
            <img
                :src="goods.img || `https://picsum.photos/200/200?random=${goods.id}`"
                :alt="goods.name"
                class="img-cover"
            />
          </div>
          <div class="product-info">
            <h3>{{ goods.name || '商品名称' }}</h3>
            <p class="price">¥{{ goods.price ? goods.price.toFixed(2) : '0.00' }}</p>
          </div>
        </div> <!-- 修正：闭合商品卡片 -->
      </div>
    </div>

    <!-- 二手专区 -->
    <div class="section">
      <h2 class="section-title">二手专区</h2>
      <div class="product-grid">
        <div
            v-for="(goods, index) in secondHandList"
            :key="goods.id || index"
            class="product-card"
            @click="goToGoodDetail(goods.id)"
        >
          <div class="product-img">
            <img
                :src="goods.img || `https://picsum.photos/200/200?random=${goods.id}`"
                :alt="goods.name"
                class="img-cover"
            />
          </div>
          <div class="product-info">
            <h3>{{ goods.name || '二手物品' }}</h3>
            <p class="price">¥{{ goods.price ? goods.price.toFixed(2) : '0.00' }}</p>
          </div>
        </div> <!-- 修正：闭合商品卡片 -->
      </div>
    </div>
  </div> <!-- 修正：闭合 home-page 容器 -->
</template>

<script setup>
// 保持原有逻辑不变
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getGuessYouLike, getSecondHandGoods } from '@/api/goods';

const router = useRouter();
const activeCategory = ref('电子产品');
const categories = ref(['电子产品', '美食', '生活', '服装']);
const guessYouLikeList = ref([]);
const secondHandList = ref([]);

// 加载商品数据
const loadGuessYouLike = async () => {
  try {
    const res = await getGuessYouLike();
    guessYouLikeList.value = res.data || [];
  } catch (error) {
    console.error('获取猜你喜欢商品失败:', error);
  }
};

const loadSecondHandGoods = async () => {
  try {
    const res = await getSecondHandGoods();
    secondHandList.value = res.data || [];
  } catch (error) {
    console.error('获取二手物品失败:', error);
  }
};

// 路由跳转方法
const changeCategory = (category) => {
  activeCategory.value = category;
  router.push({ name: 'Category', params: { type: category } });
};

const goToGoodDetail = (goodsId) => {
  if (goodsId) {
    router.push({ name: 'GoodInfo', params: { id: goodsId } });
  }
};

// 初始化
onMounted(() => {
  loadGuessYouLike();
  loadSecondHandGoods();
});
</script>

<style scoped>
/* 分类标签 */
.category-tags {
  margin-bottom: 20px;
  padding: 0 10px;
}

.tag {
  display: inline-block;
  padding: 8px 15px;
  margin-right: 10px;
  margin-bottom: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.tag:hover {
  border-color: #8a2be2;
  color: #8a2be2;
}

.tag.active {
  border-color: #8a2be2;
  color: #8a2be2;
  font-weight: bold;
}

/* 内容区块 */
.section {
  margin-bottom: 30px;
  padding: 0 10px;
}

.section-title {
  font-size: 18px;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
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

.product-img {
  height: 160px;
  background-color: #f5f5f5;
  overflow: hidden;
  position: relative;
}

.img-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 15px;
}

.product-info h3 {
  font-size: 16px;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.price {
  color: #8a2be2;
  font-weight: bold;
  font-size: 18px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>