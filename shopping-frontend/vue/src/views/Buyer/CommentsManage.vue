<template>
  <div class="shopping-cart-container">
    <!-- 页面标题 -->
    <h1 class="page-title">我的评论</h1>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 空评论提示 -->
    <div v-else-if="comments.length === 0" class="empty-cart">
      <p>你还没有发表任何评论~</p>
      <button class="back-btn" @click="goToHome">返回商品广场</button>
    </div>

    <!-- 评论列表 -->
    <div v-else>
      <div class="cart-list">
        <div
            class="cart-item"
            v-for="item in currentPageItems"
            :key="item.id"
            @click="viewProductDetail(item.goodsId)"
        >
          <!-- 商品图片 -->
          <div class="product-image">
            <img :src="item.img" :alt="item.name">
          </div>

          <div class="product-info">
            <h3 class="product-name">{{ item.name }}</h3>
            <div class="product-desc">我的评论: {{ item.content }}</div>
            <div class="product-price">¥{{ item.price.toFixed(2) }}</div>
          </div>

          <div class="action-buttons" @click.stop>
            <button
                class="action-btn remove-btn"
                @click="deleteComment(item.id)"
            >
              删除
            </button>
          </div>
        </div>
      </div>

      <!-- 分页组件 -->
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
import { getMyComments, deleteComment } from '@/api/comment';

export default {
  name: 'CommentsManage',
  data() {
    return {
      comments: [], // 所有评论
      currentPageItems: [], // 当前页显示的评论
      loading: true, // 加载状态
      userId: null, // 从localStorage读取用户ID
      currentPage: 1, // 当前页码
      pageSize: 5 // 每页显示数量
    };
  },
  computed: {
    // 计算总页数
    totalPages() {
      return Math.ceil(this.comments.length / this.pageSize);
    }
  },
  watch: {
    // 当评论列表变化时重新计算分页
    comments() {
      this.resetPagination();
    },
    // 当页码变化时更新当前页评论
    currentPage() {
      this.updateCurrentPageItems();
    }
  },
  mounted() {
    // 页面加载时获取用户ID并加载评论
    this.loadUserId();
    this.loadMyComments();
  },
  methods: {
    // 从localStorage加载用户ID
    loadUserId() {
      const storedUserId = localStorage.getItem('userId');
      if (storedUserId) {
        this.userId = storedUserId;
      } else {
        // 未登录时跳转至登录页
        alert('请先登录查看评论');
        this.$router.push({ name: 'Auth' });
      }
    },

    // 加载我的评论
    async loadMyComments() {
      // 若未获取到userId则不发起请求
      if (!this.userId) return;

      try {
        this.loading = true;
        // 使用从localStorage获取的userId
        const response = await getMyComments(this.userId);
        if (response.success) {
          this.comments = response.data || [];
        } else {
          alert('加载评论失败：' + response.message);
        }
      } catch (error) {
        console.error('加载评论出错：', error);
        alert('网络异常，无法加载评论');
      } finally {
        this.loading = false;
      }
    },

    // 重置分页
    resetPagination() {
      this.currentPage = 1;
      this.updateCurrentPageItems();
    },

    // 更新当前页显示的评论
    updateCurrentPageItems() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      this.currentPageItems = this.comments.slice(startIndex, endIndex);
    },

    // 上一页
    prevPage() {
      if (this.currentPage > 1) {
        this.currentPage--;
      }
    },

    // 下一页
    nextPage() {
      if (this.currentPage < this.totalPages) {
        this.currentPage++;
      }
    },

    // 查看商品详情
    viewProductDetail(goodsId) {
      this.$router.push({ name: 'GoodInfo', params: { id: goodsId } });
    },

    // 删除评论
    async deleteComment(commentId) {
      if (confirm('确定要删除这条评论吗？')) {
        try {
          const response = await deleteComment(commentId);
          if (response.success) {
            // 删除成功后更新列表
            this.comments = this.comments.filter(item => item.id !== commentId);
            // 如果当前页为空且不是第一页，自动跳转到上一页
            if (this.currentPageItems.length === 1 && this.currentPage > 1) {
              this.currentPage--;
            }
            alert('删除成功');
          } else {
            alert('删除失败：' + response.message);
          }
        } catch (error) {
          console.error('删除评论出错：', error);
          alert('网络异常，删除失败');
        }
      }
    },

    // 返回商品广场
    goToHome() {
      this.$router.push('/');
    }
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
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
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