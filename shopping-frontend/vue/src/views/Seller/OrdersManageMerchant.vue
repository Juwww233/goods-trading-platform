<template>
  <div class="orders-manage-container">
    <!-- 页面标题、筛选按钮和商品筛选下拉框 -->
    <div class="header-container">
      <h1 class="page-title">订单管理</h1>

      <!-- 右侧筛选区域：商品下拉框 + 状态按钮组 -->
      <div class="filter-container">
        <!-- 商品筛选下拉框 -->
        <select
            class="product-filter"
            v-model="selectedProduct"
            @change="filterByProduct"
            :disabled="loading"
        >
          <option value="all">全部商品</option>
          <option
              v-for="product in uniqueProducts"
              :key="product"
              :value="product"
          >
            {{ product }}
          </option>
        </select>

        <!-- 状态筛选按钮组 -->
        <div class="filter-buttons">
          <button
              :class="['filter-btn', currentFilter === 'unshipped' ? 'active' : '']"
              @click="filterOrders('unshipped')"
          >
            未发货
          </button>
          <button
              :class="['filter-btn', currentFilter === 'shipped' ? 'active' : '']"
              @click="filterOrders('shipped')"
          >
            已发货
          </button>
          <button
              :class="['filter-btn', currentFilter === 'all' ? 'active' : '']"
              @click="filterOrders('all')"
          >
            全部订单
          </button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading">加载中...</div>

    <!-- 错误提示 -->
    <div v-if="error" class="error">{{ error }}</div>

    <!-- 空订单提示 -->
    <div v-if="!loading && !error && filteredOrders.length === 0" class="empty">
      暂无符合条件的订单~
    </div>

    <!-- 订单列表（显示当前页的订单） -->
    <div class="orders-list" v-if="!loading && !error && filteredOrders.length > 0">
      <div
          class="order-card"
          v-for="order in currentPageOrders"
          :key="order.id"
          @click="viewOrderDetail(order.id)"
      >
        <!-- 商品图片 -->
        <div class="product-image">
          <img :src="order.goodsImg" :alt="order.goodsName">
        </div>

        <div class="order-info">
          <h3 class="product-name">{{ order.goodsName }}</h3>
          <div class="receiver-info">
            <span>收货人：{{ order.userName }}</span>
            <span class="phone">电话：{{ order.phone }}</span>
          </div>
          <div class="address-info">
            收货地址：{{ order.address || '未知地址' }}
          </div>
          <div class="order-status">
            订单状态：<span class="status-text" :class="getStatusClass(order.status)">{{ order.status }}</span>
          </div>
        </div>

        <button
            v-if="order.status === '已支付'"
            class="action-btn ship-btn"
            @click.stop="handleShip(order)"
        >
          发货
        </button>
      </div>
    </div>

    <!-- 分页组件 -->
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
import { getOrdersByName } from '@/api/orders'; // 修改：引入通过商品名称查询订单的API
import { updateOrderStatus } from '@/api/orders'; // 引入更新订单状态API
import { getGoodsByUserId } from '@/api/goods'; // 引入获取用户商品API

export default {
  name: 'OrdersManageMerchant',
  data() {
    return {
      orders: [], // 原始订单列表
      filteredOrders: [], // 筛选后的订单列表
      currentPageOrders: [], // 当前页订单
      loading: true, // 加载状态
      error: '', // 错误信息
      userId: null, // 登录用户ID（从localStorage获取）
      currentFilter: 'unshipped', // 默认筛选未发货
      currentPage: 1, // 当前页码
      pageSize: 5, // 每页数量
      selectedProduct: 'all', // 选中的商品，默认全部
      uniqueProducts: [] // 唯一商品名称列表（当前用户发布的商品）
    };
  },
  computed: {
    // 计算总页数
    totalPages() {
      return Math.ceil(this.filteredOrders.length / this.pageSize);
    }
  },
  watch: {
    // 筛选后订单变化时重置分页
    filteredOrders() {
      this.resetPagination();
    },
    // 页码变化时更新当前页订单
    currentPage() {
      this.updateCurrentPageOrders();
    }
  },
  mounted() {
    // 先加载用户ID，再加载数据
    this.loadUserId();
  },
  methods: {
    // 从localStorage加载用户ID
    loadUserId() {
      try {
        const storedUserId = localStorage.getItem('userId');
        if (storedUserId && !isNaN(Number(storedUserId))) {
          this.userId = Number(storedUserId);
          // 先加载用户商品，再根据商品名称加载订单
          this.loadUserGoods();
        } else {
          this.error = '未找到有效用户信息，请先登录';
          this.loading = false;
        }
      } catch (err) {
        this.error = '获取用户信息失败';
        this.loading = false;
        console.error('读取localStorage失败:', err);
      }
    },

    // 加载当前用户发布的商品
    async loadUserGoods() {
      try {
        const res = await getGoodsByUserId(this.userId);
        if (res.success && res.data) {
          // 提取当前用户发布的商品名称（去重）
          this.uniqueProducts = [...new Set(res.data.map(goods => goods.name))];
          // 加载订单（依赖用户商品列表）
          this.loadOrdersByGoodsNames();
        } else {
          this.error = '获取商品列表失败';
          this.loading = false;
        }
      } catch (err) {
        console.error('获取用户商品列表失败:', err);
        this.error = '获取商品列表失败，请重试';
        this.loading = false;
      }
    },

    // 修改：通过用户发布的商品名称加载订单
    async loadOrdersByGoodsNames() {
      if (!this.userId || this.uniqueProducts.length === 0) {
        this.loading = false;
        return;
      }

      try {
        this.loading = true;
        // 存储所有商品的订单
        let allOrders = [];

        // 遍历用户发布的所有商品名称，查询对应的订单
        for (const goodsName of this.uniqueProducts) {
          const response = await getOrdersByName(goodsName);
          if (response.success && response.data) {
            // 合并当前商品的订单到总列表
            allOrders = [...allOrders, ...response.data];
          }
        }

        // 去重处理（避免同一订单被多次添加）
        const uniqueOrders = [...new Map(allOrders.map(order => [order.id, order])).values()];

        // 处理订单数据
        this.orders = uniqueOrders.map(order => ({
          ...order,
          goodsImg: order.goodsImg || 'https://picsum.photos/200/200?random=1',
          goodsName: order.goodsName || '未知商品',
          userName: order.userName || '未知收货人',
          phone: order.phone || '未知电话',
          address: order.address || '未知地址'
        }));

        // 应用默认筛选
        this.filterOrders(this.currentFilter);
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

      // 先按商品筛选（仅显示当前用户发布的商品订单）
      let filteredByProduct = this.orders;
      if (this.selectedProduct !== 'all') {
        filteredByProduct = this.orders.filter(order =>
            order.goodsName === this.selectedProduct
        );
      }

      // 再按状态筛选
      if (status === 'unshipped') {
        // 未发货 = 已支付状态
        this.filteredOrders = filteredByProduct.filter(order => order.status === '已支付');
      } else if (status === 'shipped') {
        // 已发货 = 已发货 + 已收货状态
        this.filteredOrders = filteredByProduct.filter(order =>
            order.status === '已发货' || order.status === '已收货'
        );
      } else {
        // 全部订单
        this.filteredOrders = filteredByProduct;
      }

      // 重置分页
      this.resetPagination();
    },

    // 按商品筛选
    filterByProduct() {
      this.filterOrders(this.currentFilter);
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

    // 查看订单详情
    viewOrderDetail(orderId) {
      if (!orderId || isNaN(Number(orderId))) {
        console.error('无效的订单ID:', orderId);
        alert('订单ID无效，无法查看详情');
        return;
      }

      this.$router.push({
        name: 'OrderInfoMerchant',
        params: { orderId: orderId }
      });
    },

    // 处理发货操作
    async handleShip(order) {
      if (confirm('确认要发货此订单吗？')) {
        try {
          // 调用API更新订单状态为"已发货"
          const response = await updateOrderStatus(order.id, '已发货');
          if (response.success) {
            alert('发货成功！');
            // 重新加载订单并保持当前筛选状态
            await this.loadUserGoods(); // 重新加载用户商品及对应的订单
            this.filterOrders(this.currentFilter);
          } else {
            alert('发货失败：' + (response.message || '未知错误'));
          }
        } catch (err) {
          console.error('发货操作失败：', err);
          alert('网络异常，操作失败');
        }
      }
    },

    // 订单状态样式
    getStatusClass(status) {
      const classMap = {
        '已支付': 'paid',       // 未发货
        '已发货': 'shipped',     // 已发货
        '已收货': 'received',    // 已收货
        '已取消': 'cancelled'    // 已取消
      };
      return classMap[status] || '';
    }
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.header-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.product-filter {
  padding: 6px 12px;
  border: 1px solid #8a2be2;
  border-radius: 4px;
  color: #333;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
}

.address-info {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-btn.ship-btn {
  background-color: #8a2be2;
  color: white;
}

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

.filter-btn:hover:not(.active) {
  background-color: #f0e6ff;
}

.loading {
  text-align: center;
  padding: 50px 0;
  color: #666;
  font-size: 16px;
}

.error {
  text-align: center;
  padding: 50px 0;
  color: #e4393c;
  font-size: 16px;
}

.empty {
  text-align: center;
  padding: 50px 0;
  color: #666;
  font-size: 16px;
}

.orders-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
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

.phone {
  margin-left: 20px;
}

.order-status {
  font-size: 14px;
  color: #666;
}

.status-text {
  font-weight: bold;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.status-text.paid {
  color: #4caf50;
}

.status-text.shipped {
  color: #1976d2;
}

.status-text.received {
  color: #2196f3;
}

.status-text.cancelled {
  color: #999;
}

@media (max-width: 768px) {
  .header-container {
    flex-direction: column;
    align-items: flex-start;
  }

  .filter-container {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .product-filter {
    width: 100%;
    margin-bottom: 0;
  }

  .filter-buttons {
    width: 100%;
    justify-content: space-between;
  }

  .order-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .product-image {
    width: 100%;
    height: 150px;
    margin-right: 0;
  }

  .action-btn {
    align-self: flex-end;
  }
}
</style>