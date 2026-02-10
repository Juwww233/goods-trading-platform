<template>
  <div class="publish-page">
    <h2>发布二手物品</h2>
    <form class="publish-form" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label class="form-label">商品名称 <span class="required">*</span></label>
        <input
            type="text"
            v-model="form.name"
            placeholder="请输入商品名称"
            required
        >
      </div>

      <div class="form-group">
        <label class="form-label">商品价格 <span class="required">*</span></label>
        <input
            type="number"
            v-model="form.price"
            placeholder="请输入价格（元）"
            min="0"
            step="0.01"
            required
        >
      </div>

      <!-- 新增商品种类下拉栏 -->
      <div class="form-group">
        <label class="form-label">商品种类 <span class="required">*</span></label>
        <select
            v-model="form.category"
            class="category-select"
            required
        >
          <option value="二手物品">二手物品</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">商品描述 <span class="required">*</span></label>
        <textarea
            v-model="form.content"
            placeholder="请描述商品详情、新旧程度等信息"
            rows="5"
            required
        ></textarea>
      </div>

      <div class="form-group">
        <label class="form-label">交易地址</label>
        <input
            type="text"
            v-model="form.address"
            placeholder="请输入可交易的地址"
        >
      </div>

      <!-- 修改：将文件上传改为文本输入框 -->
      <div class="form-group">
        <label class="form-label">商品图片URL <span class="required">*</span></label>
        <input
            type="text"
            v-model="form.img"
            placeholder="请输入图片的URL地址"
            required
        >
        <div class="preview-container" v-if="form.img">
          <img :src="form.img" class="preview-img" alt="商品预览图" @error="handleImageError">
          <p v-if="imageError" class="error-message">图片地址无效，请检查URL是否正确</p>
        </div>
      </div>

      <div class="form-actions">
        <button type="button" class="cancel-btn" @click="goBack">取消</button>
        <button type="submit" class="submit-btn">发布商品</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { publishGoods } from '@/api/goods';

const router = useRouter();
const form = ref({
  name: '',
  price: '',
  content: '',
  address: '',
  img: '',
  category: '二手物品' // 默认为二手物品
});
const imageError = ref(false);
// 处理图片加载失败
const handleImageError = () => {
  imageError.value = true;
};

// 输入图片URL时重置错误状态
const handleImageInput = () => {
  imageError.value = false;
};

// 提交表单
const handleSubmit = async () => {
  try {
    // 从 localStorage 获取用户信息
    const userId = localStorage.getItem('userId');
    const userRole = localStorage.getItem('userRole');

    if (!form.value.img) {
      alert('请输入商品图片URL');
      return;
    }

    if (!userId) {
      alert('请先登录！');
      router.push({ name: 'Auth' });
      return;
    }

    const submitData = {
      ...form.value,
      // 保持与下拉框一致，这里可以省略显式赋值
      status: '待审核',
      saleStatus: '未上架',
      userId: userId
    };

    const res = await publishGoods(submitData);
    if (res.success) {
      alert('发布成功，等待审核通过后即可上架');
      // 默认跳转
      router.push({ name: 'MyGoodsManage' });
    } else {
      alert(res.message || '发布失败，请重试');
    }
  } catch (error) {
    console.error('发布商品失败:', error);
    alert('发布失败，请检查网络后重试');
  }
};

// 返回上一页
const goBack = () => {
  router.go(-1);
};
</script>

<style scoped>
.publish-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.publish-form {
  background-color: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.form-group {
  margin-bottom: 25px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #333;
}

.required {
  color: #ff4d4f;
}

input, textarea, .category-select {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

textarea {
  resize: vertical;
}

.preview-container {
  margin-top: 10px;
}

.preview-img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
  border: 1px solid #eee;
}

.error-message {
  color: #ff4d4f;
  font-size: 12px;
  margin-top: 5px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.cancel-btn, .submit-btn {
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

.submit-btn {
  background-color: #8a2be2;
  color: white;
  border: none;
}

.submit-btn:hover {
  background-color: #7a00d9;
}
</style>
