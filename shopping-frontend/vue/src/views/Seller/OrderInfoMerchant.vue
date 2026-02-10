<template>
  <div class="order-container">
    <!-- 订单标题（添加返回按钮） -->
    <div class="order-header">
      <h2 class="order-title">订单信息</h2>
      <button class="back-button" @click="goBack">返回</button>
    </div>

    <!-- 商品信息区域 -->
    <div class="product-info">
      <h3>商品信息</h3>
      <div class="product-detail">
        <div class="product-image">
          <img :src="goodsImg" :alt="goodsName">
        </div>
        <div class="product-text">
          <div class="input-group">
            <label>商品名称：</label>
            <span class="fixed-value">{{ goodsName }}</span>
          </div>
          <div class="input-group">
            <label>数量：</label>
            <span class="fixed-value">{{ count }}</span> <!-- 数量从数据读取，不可编辑 -->
          </div>
          <div class="input-group">
            <label>单价：</label>
            <span class="fixed-value">{{ goodsPrice.toFixed(2) }}元</span>
          </div>
          <div class="input-group">
            <label>总价：</label>
            <span class="fixed-value">{{ total.toFixed(2) }}元</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 收货信息区域 -->
    <div class="shipping-info">
      <h3>收货信息</h3>
      <div class="info-item">
        <label>收货地址：</label>
        <span class="fixed-value">{{ address || '暂无地址信息' }}</span> <!-- 从数据读取 -->
      </div>
      <div class="info-item">
        <label>收货人：</label>
        <span class="fixed-value">{{ userName || '暂无收货人信息' }}</span> <!-- 从数据读取 -->
      </div>
      <div class="info-item">
        <label>电话号码：</label>
        <span class="fixed-value">{{ phone || '暂无联系方式' }}</span> <!-- 从数据读取 -->
      </div>
    </div>

    <!-- 订单状态区域 -->
    <div class="order-footer">
      <div class="order-status">订单状态：{{ status }}</div>
      <!-- 移除支付按钮，状态从数据读取 -->
    </div>
  </div>
</template>

<script>
import { getOrderById } from '@/api/orders'; // 新增：获取订单详情的API

export default {
  name: 'OrderInfo',
  data() {
    return {
      // 商品基础信息
      goodsName: '',
      goodsImg: '',
      goodsPrice: 0,
      count: 1,

      // 订单计算字段
      total: 0,
      status: '待支付', // 从数据读取

      // 收货信息（从数据读取，不可编辑）
      address: '',
      userName: '',
      phone: '',

      // 订单信息
      orderNo: '',
      orderId: 0, // 订单ID，从路由参数获取

      // 用户与卖家信息
      userId: 1,
      saleId: 0
    };
  },
// OrderInfo.vue 的created或mounted钩子
  created() {
    // 从params获取orderId，与路由配置和跳转参数一致
    this.orderId = this.$route.params.orderId || 0;
    if (!this.orderId) {
      alert('订单ID不存在');
      this.goBack();
      return;
    }
    this.loadOrderInfo(); // 加载订单详情
  },
  methods: {
    // 加载订单详情（从数据库读取）
    async loadOrderInfo() {
      // 从路由query正确获取订单ID（已匹配传递方式）
      //this.orderId = this.$route.query.orderId || 0;
      if (!this.orderId) {
        alert('订单ID不存在');
        this.goBack(); // 无ID时返回上一页
        return;
      }

      try {
        const response = await getOrderById(this.orderId);
        if (response.success && response.data) {
          const orderData = response.data;
          // 填充订单数据，并添加默认值避免"暂无"
          this.goodsName = orderData.goodsName || '未知商品';
          this.goodsImg = orderData.goodsImg || 'https://picsum.photos/200/200?random=1';
          this.goodsPrice = Number(orderData.goodsPrice) || 0;
          this.count = Number(orderData.count) || 1;
          this.total = Number(orderData.total) || 0;
          this.status = orderData.status || '待支付'; // 确保状态正确

          // 收货信息默认值优化
          this.address = orderData.address || '暂无地址信息';
          this.userName = orderData.userName || '暂无收货人信息';
          this.phone = orderData.phone || '暂无联系方式';

          this.orderNo = orderData.orderNo || '未知订单号';
          this.userId = orderData.userId || 0;
          this.saleId = orderData.saleId || 0;
        } else {
          alert('获取订单信息失败：' + (response.message || '未知错误'));
        }
      } catch (error) {
        console.error('加载订单详情失败：', error);
        alert('网络异常，无法加载订单信息');
      }
    },

    // 从路由参数加载商品信息（兼容无订单ID的场景）
    loadProductFromRoute() {
      const routeQuery = this.$route.query;
      this.goodsName = routeQuery.name || '未知商品';
      this.goodsImg = routeQuery.img || 'https://picsum.photos/200/200?random=1';
      this.goodsPrice = Number(routeQuery.price) || 0;
      this.saleId = Number(routeQuery.saleId) || 1001;
      this.count = Number(routeQuery.count) || 1;
      // 计算总价
      this.total = this.goodsPrice * this.count;
      this.status = '待支付'; // 默认状态
    },

    // 返回上一页
    goBack() {
      this.$router.go(-1);
    }
  }
};
</script>

<style scoped>
/* 保持原有样式，移除支付方式相关样式 */
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.back-button {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.back-button:hover {
  background: #f5f5f5;
}

.order-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.order-title {
  color: #333;
  font-size: 24px;
  margin: 0;
}

.product-info h3,
.shipping-info h3 {
  color: #666;
  font-size: 18px;
  margin: 15px 0;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.product-detail {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}

.product-image {
  width: 300px;
  height: 200px;
  background: #e0e0e0;
  margin-right: 20px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.input-group {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}

.input-group label {
  width: 80px;
  color: #888;
}

.fixed-value {
  padding: 8px 0;
  color: #333;
}

.info-item {
  margin: 12px 0;
  padding-left: 15px;
  display: flex;
  align-items: center;
}

.info-item label {
  color: #888;
  width: 80px;
}

.order-footer {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.order-status {
  color: #e4393c;
  font-weight: bold;
  font-size: 16px;
}
</style>