<template>
  <div class="user-profile">
    <h2>个人信息</h2>

    <form class="profile-form">
      <!-- 原有表单内容保持不变 -->
      <div class="form-group">
        <label class="form-label">头像</label>
        <div class="avatar-container">
          <div
              class="avatar"
              :style="{ backgroundColor: avatarColor }"
              v-if="!avatar"
          >
            {{ username.charAt(0) }}
          </div>
          <img
              v-else
              :src="avatar"
              alt="用户头像"
              class="avatar-img"
          >
          <div class="avatar-upload" v-if="editing">
            <input
                type="file"
                @change="handleAvatarUpload"
                accept="image/*"
            >
          </div>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">用户名 <span class="required">*</span></label>
        <span v-if="!editing">{{ username }}</span>
        <input
            v-else
            v-model="form.username"
            type="text"
            disabled
        >
      </div>

      <div class="form-group">
        <label class="form-label">姓名 <span class="required">*</span></label>
        <span v-if="!editing">{{ userInfo.name || '未设置' }}</span>
        <input
            v-else
            v-model="form.name"
            type="text"
            required
        >
      </div>

      <div class="form-group">
        <label class="form-label">电话 <span class="required">*</span></label>
        <span v-if="!editing">{{ userInfo.phone || '未设置' }}</span>
        <input
            v-else
            v-model="form.phone"
            type="tel"
            required
        >
      </div>

      <div class="form-group">
        <label class="form-label">身份</label>
        <span>{{ userInfo.role === 'buyer' ? '普通用户' : '商家' }}</span>
      </div>

      <div class="form-actions">
        <button
            type="button"
            class="cancel-btn"
            @click="editing ? cancelEdit() : goBack()"
        >
          {{ editing ? '取消' : '返回' }}
        </button>

        <button
            type="button"
            class="submit-btn"
            @click="editing ? saveProfile() : enterEditMode()"
        >
          {{ editing ? '保存' : '编辑' }}
        </button>
      </div>
    </form>

    <!-- 修改密码区域：直接显示在页面中 -->
    <div class="security-section">
      <h3>账户安全</h3>
      <div class="password-form-container">
        <h4>修改密码</h4>

        <div class="password-form">
          <div class="form-group">
            <label class="form-label">原密码 <span class="required">*</span></label>
            <input
                type="password"
                v-model="passwordForm.oldPassword"
                placeholder="请输入原密码"
                required
            >
          </div>

          <div class="form-group">
            <label class="form-label">新密码 <span class="required">*</span></label>
            <input
                type="password"
                v-model="passwordForm.newPassword"
                placeholder="请输入新密码（至少6位）"
                minlength="6"
                required
            >
          </div>

          <div class="form-group">
            <label class="form-label">确认新密码 <span class="required">*</span></label>
            <input
                type="password"
                v-model="passwordForm.confirmPassword"
                placeholder="请再次输入新密码"
                minlength="6"
                required
            >
          </div>

          <div class="form-actions">
            <button
                type="button"
                class="cancel-btn"
                @click="resetPasswordForm"
            >
              重置
            </button>
            <button
                type="button"
                class="submit-btn"
                @click="handleChangePassword"
            >
              确认修改
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getCurrentUser, updateUserInfo, uploadAvatar, changePassword } from '@/api/user';
import api from '@/api/index';

// 路由和状态管理
const router = useRouter();
const editing = ref(false);

// 表单数据
const form = ref({
  username: '',
  name: '',
  phone: ''
});

// 密码表单数据
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

// 密码验证状态
const passwordMismatch = ref(false);

// 监听密码输入，验证一致性
watch([() => passwordForm.value.newPassword, () => passwordForm.value.confirmPassword], () => {
  passwordMismatch.value = passwordForm.value.newPassword !== passwordForm.value.confirmPassword;
});

// 用户信息存储
const userInfo = ref({});
const avatar = ref('');
const username = computed(() => localStorage.getItem('username') || '');

// 动态头像背景色
const avatarColor = computed(() => {
  const colors = ['#8a2be2', '#ff6347', '#40e0d0', '#ffa500', '#9932cc'];
  return colors[username.value.length % colors.length];
});

// 获取用户信息（保持不变）
const fetchUserInfo = async () => {
  try {
    const userId = localStorage.getItem('userId');
    const res = await getCurrentUser(userId);
    if (res.success) {
      userInfo.value = res.data;
      form.value = {
        username: username.value,
        name: res.data.name || '',
        phone: res.data.phone || ''
      };
      avatar.value = res.data.avatar || '';
    } else {
      // 替换为 confirm
      confirm(res.message || '获取用户信息失败');
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
    // 替换为 confirm
    confirm('获取信息失败，请检查网络');
  }
};

// 进入编辑模式（保持不变）
const enterEditMode = () => {
  editing.value = true;
};

// 保存个人信息（保持不变）
const saveProfile = async () => {
  try {
    const userId = localStorage.getItem('userId');
    if (!userId) throw new Error('用户未登录');

    if (!form.value.name.trim()) {
      // 替换为 confirm
      confirm('请输入姓名');
      return;
    }

    if (!form.value.phone.trim()) {
      // 替换为 confirm
      confirm('请输入电话');
      return;
    }

    const updateData = {
      id: userId,
      name: form.value.name,
      phone: form.value.phone
    };

    const res = await updateUserInfo(updateData);
    if (res.success) {
      userInfo.value = { ...userInfo.value, ...updateData };
      editing.value = false;
      // 替换为 confirm
      confirm('个人信息已更新');
    } else {
      // 替换为 confirm
      confirm(res.message || '更新失败，请重试');
    }
  } catch (error) {
    console.error('保存个人信息错误:', error);
    // 替换为 confirm
    confirm(error.message || '保存失败');
  }
};

// 取消编辑（保持不变）
const cancelEdit = () => {
  form.value = {
    username: username.value,
    name: userInfo.value.name || '',
    phone: userInfo.value.phone || ''
  };
  editing.value = false;
};

// 头像上传（保持不变）
const handleAvatarUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  try {
    const formData = new FormData();
    formData.append('avatar', file);
    formData.append('userId', localStorage.getItem('userId'));

    const res = await uploadAvatar(formData);
    if (res.success) {
      avatar.value = res.data.avatar;
      userInfo.value.avatar = res.data.avatar;
      // 替换为 confirm
      confirm('头像更新成功');
    } else {
      // 替换为 confirm
      confirm(res.message || '上传失败');
    }
  } catch (error) {
    console.error('上传头像错误:', error);
    // 替换为 confirm
    confirm('上传失败，请检查网络');
  }
};

// 重置密码表单
const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
};

// 处理修改密码逻辑
const handleChangePassword = async () => {
  // 表单验证
  if (!passwordForm.value.oldPassword) {
    // 替换为 confirm
    confirm('请输入原密码');
    return;
  }

  if (passwordForm.value.newPassword.length < 6) {
    // 替换为 confirm
    confirm('新密码长度不能少于6位');
    return;
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    // 替换为 confirm
    confirm('两次输入的密码不一致');
    return;
  }

  try {
    const sessionId = localStorage.getItem('sessionId');
    const res = await api.put('/user/changePassword',
        {
          oldPassword: passwordForm.value.oldPassword,
          newPassword: passwordForm.value.newPassword
        },
        {
          headers: {
            'X-Session-Id': sessionId,
            'Content-Type': 'application/json'
          }
        }
    );

    if (res.success) {
      // 替换为 confirm
      confirm('密码修改成功');
      resetPasswordForm(); // 重置表单
    } else {
      // 替换为 confirm
      confirm(res.message || '修改失败，请检查原密码是否正确');
    }
  } catch (error) {
    console.error('修改密码错误:', error);
    // 替换为 confirm
    confirm('修改失败，请检查网络');
  }
};

// 返回上一页（保持不变）
const goBack = () => {
  router.go(-1);
};

// 页面加载时获取用户信息（保持不变）
onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
/* 原有样式保持不变 */
.user-profile {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.user-profile h2 {
  color: #333;
  margin-bottom: 30px;
  font-size: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.profile-form {
  background-color: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 30px; /* 增加与下方区域的间距 */
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

.avatar-container {
  display: flex;
  align-items: center;
  gap: 15px;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  font-weight: bold;
}

.avatar-img {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #eee;
}

.avatar-upload input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.profile-form input,
.password-form input { /* 统一输入框样式 */
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 30px;
}

.submit-btn, .cancel-btn {
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.submit-btn {
  background-color: #8a2be2;
  color: white;
  border: none;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
}

/* 密码修改区域样式 */
.security-section {
  background-color: white;
  padding: 20px 30px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.security-section h3 {
  margin-top: 0;
  color: #333;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.password-form-container {
  padding: 20px 0;
}

.password-form-container h4 {
  color: #333;
  margin: 0 0 20px 0;
  font-size: 16px;
}

.password-form {
  max-width: 500px; /* 限制密码表单宽度 */
}

.password-form .form-group {
  margin-bottom: 20px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .password-form {
    max-width: 100%;
  }
}
</style>