<template>
  <div class="shopping-cart-container">
    <h1 class="page-title">购物车</h1>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="cartItems.length === 0" class="empty-cart">
      <p>购物车是空的，快去添加商品吧~</p>
      <button class="back-btn" @click="goToHome">返回商品广场</button>
    </div>

    <div v-else>
      <div class="cart-list">
        <div
            class="cart-item"
            v-for="item in currentPageItems"
            :key="item.id"
            @click="viewProductDetail(item.id)"
        >
          <div class="product-image">
            <img :src="item.img" :alt="item.name">
          </div>

          <div class="product-info">
            <h3 class="product-name">{{ item.name }}</h3>
            <div class="product-desc">{{ item.description }}</div>
            <div class="product-price">¥{{ item.price.toFixed(2) }}</div>
          </div>

          <div class="action-buttons" @click.stop>
            <button
                class="action-btn remove-btn"
                @click="removeFromCart(item.id)"
            >
              删除
            </button>
            <button
                class="action-btn buy-btn"
                @click="buyNow(item)"
            >
              购买
            </button>
          </div>
        </div>
      </div>

      <div class="pagination">
        <button
            class="page-btn"
            @click="prevPage"
            :disabled="currentPage === 1"
        >
          上一页
        </button>
        <span class="page-info">
          第 {{ currentPage }} / {{ totalPages }} 页
        </span>
        <button
            class="page-btn"
            @click="nextPage"
            :disabled="currentPage === totalPages"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { getCartItems, removeFromCart } from '@/api/collect';

export default {
  name: 'ShoppingCart',
  data() {
    return {
      cartItems: [],
      currentPageItems: [],
      loading: true,
      userId: null, // 从localStorage读取
      currentPage: 1,
      pageSize: 5
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.cartItems.length / this.pageSize);
    }
  },
  watch: {
    cartItems() {
      this.resetPagination();
    },
    currentPage() {
      this.updateCurrentPageItems();
    }
  },
  methods: {
    // 加载用户ID
    loadUserId() {
      const storedUserId = localStorage.getItem('userId');
      if (storedUserId) {
        this.userId = storedUserId;
      } else {
        alert('请先登录查看购物车');
        this.$router.push({ name: 'Auth' });
      }
    },

    // 加载购物车商品
    async loadCartItems() {
      if (!this.userId) return;

      try {
        this.loading = true;
        const response = await getCartItems(this.userId); // 假设API需要userId参数
        if (response.success) {
          this.cartItems = response.data || [];
        } else {
          alert('加载购物车失败：' + response.message);
        }
      } catch (error) {
        console.error('加载购物车出错：', error);
        alert('网络异常，无法加载购物车');
      } finally {
        this.loading = false;
      }
    },

    // 重置分页
    resetPagination() {
      this.currentPage = 1;
      this.updateCurrentPageItems();
    },

    // 更新当前页商品
    updateCurrentPageItems() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      this.currentPageItems = this.cartItems.slice(startIndex, endIndex);
    },

    // 分页控制
    prevPage() {
      if (this.currentPage > 1) this.currentPage--;
    },
    nextPage() {
      if (this.currentPage < this.totalPages) this.currentPage++;
    },

    // 查看商品详情
    viewProductDetail(productId) {
      this.$router.push({ name: 'GoodInfo', params: { id: productId } });
    },

    // 移除商品
    async removeFromCart(productId) {
      if (confirm('确定要移除该商品吗？')) {
        try {
          const response = await removeFromCart(productId, this.userId); // 传递userId
          if (response.success) {
            this.cartItems = this.cartItems.filter(item => item.id !== productId);
            if (this.currentPageItems.length === 1 && this.currentPage > 1) {
              this.currentPage--;
            }
            alert('移除成功');
          } else {
            alert('移除失败：' + response.message);
          }
        } catch (error) {
          console.error('移除商品出错：', error);
          alert('网络异常，移除失败');
        }
      }
    },

    // 立即购买
    buyNow(item) {
      this.$router.push({
        name: 'Order',
        params: { id: item.id },
        query: {
          name: item.name,
          img: item.img,
          price: item.price,
          saleId: item.saleId || 1001
        }
      });
    },

    // 返回首页
    goToHome() {
      this.$router.push('/');
    }
  },
  mounted() {
    this.loadUserId();
    this.loadCartItems();
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 15px;
  padding: 20px 0;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #8a2be2;
  background-color: transparent;
  color: #8a2be2;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.page-btn:hover:not(:disabled) {
  background-color: #f0e6ff;
}

.page-btn:disabled {
  color: #ccc;
  border-color: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: #666;
}

.loading {
  text-align: center;
  padding: 50px 0;
  color: #666;
  font-size: 16px;
}

.empty-cart {
  text-align: center;
  padding: 50px 0;
  color: #666;
}

.back-btn {
  margin-top: 20px;
  padding: 8px 16px;
  background-color: #8a2be2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.back-btn:hover {
  background-color: #7b1fa2;
}

.shopping-cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'PingFang SC', sans-serif;
  background-color: #fff;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 30px;
  color: #333;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #7b1fa2;
  border-radius: 4px;
  padding: 20px;
  transition: all 0.3s;
  cursor: pointer;
}

.cart-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transform: translateY(-2px);
}

.product-image {
  width: 80px;
  height: 80px;
  margin-right: 20px;
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
}

.product-name {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.product-price {
  font-size: 16px;
  color: #e4393c;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  white-space: nowrap;
}

.remove-btn {
  background-color: #f5f5f5;
  color: #333;
}

.remove-btn:hover {
  background-color: #e8e8e8;
  color: #8a2be2;
}

.buy-btn {
  background-color: #8a2be2;
  color: white;
}

.buy-btn:hover {
  background-color: #7b1fa2;
}

@media (max-width: 768px) {
  .cart-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .product-image {
    width: 100%;
    height: 150px;
    margin-right: 0;
    margin-bottom: 15px;
  }

  .action-buttons {
    width: 100%;
    flex-direction: row;
    justify-content: flex-end;
  }

  .action-btn {
    width: 48%;
  }

  .pagination {
    flex-wrap: wrap;
  }
}
</style>