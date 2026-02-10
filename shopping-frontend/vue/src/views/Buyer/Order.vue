<template>
  <div class="order-container">
    <!-- 订单标题 -->
    <div class="order-header">
      <h2 class="order-title">订单信息</h2>
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
            <!-- 关键修改：增加@change事件，确保所有数量变化都能触发计算 -->
            <input
                type="number"
                v-model.number="count"
                @input="calculateTotal"
                @change="calculateTotal"
                min="1"
                step="1"
            >
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

    <!-- 其他区域保持不变 -->
    <div class="shipping-info">
      <h3>收货信息</h3>
      <div class="info-item">
        <label>收货地址：</label>
        <input type="text" v-model="address" placeholder="请输入详细收货地址">
      </div>
      <div class="info-item">
        <label>收货人：</label>
        <input type="text" v-model="userName" placeholder="请输入收货人姓名">
      </div>
      <div class="info-item">
        <label>电话号码：</label>
        <input type="tel" v-model="phone" placeholder="请输入联系电话">
      </div>
    </div>

    <div class="payment-method">
      <h3>支付方式</h3>
      <div class="radio-group">
        <label class="radio-option">
          <input type="radio" name="payment" v-model="paymentMethod" value="wechat" checked>
          <span>微信支付</span>
        </label>
        <label class="radio-option">
          <input type="radio" name="payment" v-model="paymentMethod" value="alipay">
          <span>支付宝支付</span>
        </label>
      </div>
    </div>

    <div class="order-footer">
      <div class="order-status">订单状态：{{ status }}</div>
      <button class="pay-button" @click="submitOrder">支付</button>
    </div>
  </div>
</template>

<script>
import { createOrder } from '@/api/orders';
import { useRouter } from 'vue-router';
import { ref, watch, onMounted } from 'vue'; // 引入Vue3的组合式API

export default {
  name: 'OrderPage',
  setup() {
    const router = useRouter();

    // 改用ref定义响应式变量（关键修改）
    const goodsName = ref('');
    const goodsImg = ref('');
    const goodsPrice = ref(0);
    const count = ref(1);
    const total = ref(0);
    const status = ref('待支付');
    const address = ref('');
    const userName = ref('');
    const phone = ref('');
    const paymentMethod = ref('wechat');
    const payNo = ref('');
    const payTime = ref('');
    const userId = ref('');
    const saleId = ref(0);
    const orderNo = ref('');

    // 计算总价的方法
    const calculateTotal = () => {
      // 强制确保数量不小于1
      if (count.value < 1) {
        count.value = 1;
      }
      // 重新计算总价（使用.value访问ref变量）
      total.value = Number(goodsPrice.value) * Number(count.value);
    };

    // 从路由参数加载商品信息
    const loadProductFromRoute = () => {
      const routeQuery = router.currentRoute.value.query;
      goodsName.value = routeQuery.name || '未知商品';
      goodsImg.value = routeQuery.img || 'https://picsum.photos/200/200?random=1';
      goodsPrice.value = Number(routeQuery.price) || 0;
      saleId.value = Number(routeQuery.saleId) || 1001;

      // 初始化总价
      calculateTotal();
    };

    // 从localStorage加载用户ID
    const loadUserIdFromStorage = () => {
      const storedUserId = localStorage.getItem('userId');
      if (storedUserId) {
        userId.value = storedUserId;
      } else {
        alert('请先登录再创建订单');
        router.push({ name: 'Auth' });
      }
    };

    // 验证手机号
    const validatePhone = (phoneNum) => {
      return /^1[3-9]\d{9}$/.test(phoneNum);
    };

    // 格式化日期
    const formatDate = (date) => {
      const d = new Date(date);
      return d.toISOString().slice(0, 19).replace('T', ' ');
    };

    // 提交订单
    const submitOrder = async () => {
      if (!userId.value) {
        alert('请先登录');
        router.push({ name: 'Auth' });
        return;
      }

      if (!validatePhone(phone.value)) {
        alert('请输入有效的11位手机号码');
        return;
      }

      if (!address.value.trim()) {
        alert('请填写收货地址');
        return;
      }
      if (!userName.value.trim()) {
        alert('请填写收货人姓名');
        return;
      }

      try {
        const orderData = {
          goodsName: goodsName.value,
          goodsImg: goodsImg.value,
          goodsPrice: goodsPrice.value,
          count: count.value,
          total: total.value,
          time: formatDate(new Date()),
          payNo: 'PAY' + Date.now(),
          payTime: formatDate(new Date()),
          userId: userId.value,
          address: address.value,
          phone: phone.value,
          userName: userName.value,
          saleId: saleId.value,
          status: '已支付'
        };

        console.log("提交的订单数据：", orderData);
        const response = await createOrder(orderData);

        if (response.success && response.data.id) {
          if (confirm(`订单创建成功！订单号: ${response.data.orderNo}`)) {
            router.push('/ordersManage');
          }
        } else {
          alert('订单创建失败: ' + (response.message || '未知错误'));
        }
      } catch (error) {
        console.error('订单提交失败:', error);
        alert('订单提交失败: ' + (error.message || '网络异常'));
      }
    };

    // 页面加载时初始化
    onMounted(() => {
      orderNo.value = 'ORD' + Date.now() + Math.floor(Math.random() * 1000);
      loadProductFromRoute();
      loadUserIdFromStorage();
    });

    // 监听count和goodsPrice变化（关键修改：使用组合式API的watch）
    watch([count, goodsPrice], calculateTotal);

    return {
      goodsName,
      goodsImg,
      goodsPrice,
      count,
      total,
      status,
      address,
      userName,
      phone,
      paymentMethod,
      payNo,
      payTime,
      userId,
      saleId,
      orderNo,
      calculateTotal,
      submitOrder
    };
  }
};
</script>

<style scoped>
/* 样式保持不变 */
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
}

.order-title {
  color: #333;
  font-size: 24px;
  margin: 0;
}

.product-info h3,
.shipping-info h3,
.payment-method h3 {
  color: #666;
  font-size: 18px;
  margin: 15px 0;
}

.product-detail {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
}
.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.product-image {
  width: 300px;
  height: 200px;
  background: #e0e0e0;
  margin-right: 20px;
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
}

.input-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 200px;
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

.info-item input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 300px;
}

.radio-group {
  padding: 10px 15px;
}

.radio-option {
  display: block;
  margin: 10px 0;
  cursor: pointer;
}

.radio-option input {
  margin-right: 8px;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.order-status {
  color: #e4393c;
  font-weight: bold;
}

.pay-button {
  background: #e4393c;
  color: white;
  border: none;
  padding: 10px 30px;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.pay-button:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}
</style>
