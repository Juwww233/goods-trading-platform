<template>
  <div class="orders-manage-container">
    <div class="header-container">
      <h1 class="page-title">我的订单</h1>

      <div class="filter-buttons">
        <button
            :class="['filter-btn', currentFilter === 'all' ? 'active' : '']"
            @click="filterOrders('all')"
        >
          全部订单
        </button>
        <button
            :class="['filter-btn', currentFilter === '已支付' ? 'active' : '']"
            @click="filterOrders('已支付')"
        >
          已支付
        </button>
        <button
            :class="['filter-btn', currentFilter === '已发货' ? 'active' : '']"
            @click="filterOrders('已发货')"
        >
          已发货
        </button>
        <button
            :class="['filter-btn', currentFilter === '已收货' ? 'active' : '']"
            @click="filterOrders('已收货')"
        >
          已收货
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="!loading && !error && filteredOrders.length === 0" class="empty">
      暂无符合条件的订单~
    </div>

    <div class="orders-list" v-if="!loading && !error && filteredOrders.length > 0">
      <div
          class="order-card"
          v-for="order in currentPageOrders"
          :key="order.id"
          @click="viewOrderDetail(order.id)"
      >
        <div class="product-image">
          <img :src="order.goodsImg" :alt="order.goodsName">
        </div>

        <div class="order-info">
          <h3 class="product-name">{{ order.goodsName }}</h3>
          <div class="receiver-info">
            <span>收货人：{{ order.userName }}</span>
            <span class="phone">电话：{{ order.phone }}</span>
          </div>
          <div class="order-status">
            订单状态：<span class="status-text" :class="getStatusClass(order.status)">{{ order.status }}</span>
          </div>
        </div>

        <button
            class="action-btn"
            :class="getActionClass(order.status)"
            @click.stop="handleAction(order)"
        >
          {{ getActionText(order.status) }}
        </button>
      </div>
    </div>

    <div class="pagination" v-if="!loading && !error && filteredOrders.length > 0">
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
</template>

<script>
import { getUserOrders } from '@/api/orders';
import { confirmReceipt } from '@/api/orders';
import { getGoodsIdByName } from '@/api/goods';

export default {
  name: 'OrdersManage',
  data() {
    return {
      orders: [],
      filteredOrders: [],
      currentPageOrders: [],
      loading: true,
      error: '',
      userId: null, // 从localStorage读取
      currentFilter: 'all',
      currentPage: 1,
      pageSize: 5
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.filteredOrders.length / this.pageSize);
    }
  },
  watch: {
    filteredOrders() {
      this.resetPagination();
    },
    currentPage() {
      this.updateCurrentPageOrders();
    }
  },
  methods: {
    // 加载用户ID
    loadUserId() {
      const storedUserId = localStorage.getItem('userId');
      if (storedUserId) {
        this.userId = storedUserId;
      } else {
        this.error = '请先登录查看订单';
        this.$router.push({ name: 'Auth' });
      }
    },

    // 加载订单列表
    async loadUserOrders() {
      if (!this.userId) return;

      try {
        this.loading = true;
        const response = await getUserOrders(this.userId);
        if (response.success && response.data) {
          this.orders = response.data.map(order => ({
            ...order,
            goodsImg: order.goodsImg || 'https://picsum.photos/200/200?random=1',
            goodsName: order.goodsName || '未知商品',
            userName: order.userName || '未知收货人',
            phone: order.phone || '未知电话'
          }));
          this.filterOrders(this.currentFilter);
        } else {
          this.error = '获取订单失败：' + (response.message || '未知错误');
        }
      } catch (err) {
        console.error('加载订单失败：', err);
        this.error = '网络异常，无法加载订单';
      } finally {
        this.loading = false;
      }
    },

    // 筛选订单
    filterOrders(status) {
      this.currentFilter = status;
      this.filteredOrders = status === 'all'
          ? [...this.orders]
          : this.orders.filter(order => order.status === status);
      this.resetPagination();
    },

    // 重置分页
    resetPagination() {
      this.currentPage = 1;
      this.updateCurrentPageOrders();
    },

    // 更新当前页订单
    updateCurrentPageOrders() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      this.currentPageOrders = this.filteredOrders.slice(startIndex, endIndex);
    },

    // 分页控制
    prevPage() {
      if (this.currentPage > 1) this.currentPage--;
    },
    nextPage() {
      if (this.currentPage < this.totalPages) this.currentPage++;
    },

    // 查看订单详情
    viewOrderDetail(orderId) {
      if (!orderId || isNaN(Number(orderId))) {
        alert('订单ID无效');
        return;
      }
      this.$router.push({
        name: 'OrderInfo',
        params: { orderId: orderId }
      });
    },

    // 处理订单操作
    handleAction(order) {
      if (order.status === '已支付' || order.status === '已发货') {
        this.confirmReceipt(order);
      } else if (order.status === '已收货') {
        this.writeReview(order);
      }
    },

    // 确认收货
    async confirmReceipt(order) {
      if (order.status === '已支付') {
        alert('商家还未发货哦！');
        return;
      }
      if (order.status === '已发货' && confirm('确认已收到商品吗？')) {
        try {
          const response = await confirmReceipt(order.id);
          if (response.success) {
            alert('确认收货成功！');
            await this.loadUserOrders();
            this.filterOrders(this.currentFilter);
          } else {
            alert('确认收货失败：' + (response.message || '未知错误'));
          }
        } catch (err) {
          console.error('确认收货失败：', err);
          alert('网络异常，操作失败');
        }
      }
    },

    // 写评论
    async writeReview(order) {
      if (!order.goodsName) {
        alert('无法获取商品信息');
        return;
      }
      try {
        const response = await getGoodsIdByName(order.goodsName);
        if (response.success && response.data) {
          this.$router.push({
            name: 'GoodInfo',
            params: { id: response.data }
          });
        } else {
          alert('未找到对应的商品信息');
        }
      } catch (err) {
        console.error('查询商品ID失败：', err);
        alert('查询商品信息失败，请重试');
      }
    },

    // 状态样式辅助方法
    getStatusClass(status) {
      const classMap = {
        '待支付': 'pending',
        '已支付': 'paid',
        '已发货': 'shipped',
        '已收货': 'received',
        '已取消': 'cancelled'
      };
      return classMap[status] || '';
    },
    getActionText(status) {
      const textMap = {
        '待支付': '去支付',
        '已支付': '确认收货',
        '已发货': '确认收货',
        '已收货': '写评论',
        '已取消': '重新购买'
      };
      return textMap[status] || '查看详情';
    },
    getActionClass(status) {
      const classMap = {
        '待支付': 'pay-btn',
        '已支付': 'confirm-btn',
        '已发货': 'confirm-btn',
        '已收货': 'review-btn',
        '已取消': 'reset-btn'
      };
      return classMap[status] || 'default-btn';
    }
  },
  mounted() {
    this.loadUserId();
    this.loadUserOrders();
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.header-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.filter-buttons {
  display: flex;
  gap: 10px;
}

.filter-btn {
  padding: 6px 12px;
  border: 1px solid #8a2be2;
  background-color: transparent;
  color: #8a2be2;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.filter-btn.active {
  background-color: #8a2be2;
  color: white;
}

.loading, .error, .empty {
  text-align: center;
  padding: 50px 0;
  font-size: 16px;
}

.error { color: #e4393c; }
.empty { color: #666; }

.orders-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'PingFang SC', sans-serif;
  background-color: #fff;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #7b1fa2;
  border-radius: 4px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s;
}

.order-card:hover {
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

.order-info {
  flex: 1;
}

.product-name {
  font-size: 18px;
  margin-bottom: 10px;
  color: #333;
}

.receiver-info {
  display: flex;
  gap: 20px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.phone { margin-left: 20px; }

.order-status {
  font-size: 14px;
  color: #666;
}

.status-text { font-weight: bold; }
.status-text.pending { color: #ff9900; }
.status-text.paid { color: #4caf50; }
.status-text.shipped { color: #1976d2; }
.status-text.received { color: #2196f3; }
.status-text.cancelled { color: #999; }

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.action-btn.pay-btn { background-color: #e4393c; color: white; }
.action-btn.confirm-btn { background-color: #8a2be2; color: white; }
.action-btn.review-btn { background-color: #ff6600; color: white; }
.action-btn.reset-btn { background-color: #999; color: white; }

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 30px;
  gap: 15px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #8a2be2;
  background-color: transparent;
  color: #8a2be2;
  border-radius: 4px;
  cursor: pointer;
}

.page-btn:hover:not(:disabled) { background-color: #f0e6ff; }
.page-btn:disabled { color: #ccc; border-color: #ccc; cursor: not-allowed; }

@media (max-width: 768px) {
  .header-container { flex-direction: column; align-items: flex-start; }
  .filter-buttons { width: 100%; justify-content: space-between; margin-top: 10px; }
  .order-card { flex-direction: column; align-items: flex-start; gap: 15px; }
  .product-image { width: 100%; height: 150px; margin-right: 0; }
  .action-btn { align-self: flex-end; }
}
</style>