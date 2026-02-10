// @/api/user.js
import api from './index';

// 用户登录
export const login = (userName, password) =>
    api.post('/user/login', { userName, password });

// 用户注册
export const register = (userData) =>
    api.post('/user/register', userData);

// 获取当前登录用户信息
export const getCurrentUser = (userId) =>
    api.get(`/user/${userId}`);

// 退出登录
export const logout = () =>
    api.post('/user/logout');

// 更新用户信息
export const updateUserInfo = (data) =>
    api.post('/user/update', data);

// 上传头像
export const uploadAvatar = (formData) =>
    api.post('/user/uploadAvatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });

// 新增：修改密码
export const changePassword = (oldPassword, newPassword) =>
    api.put('/user/changePassword', {
        oldPassword,
        newPassword
    }, {
        headers: {
            'X-Session-Id': localStorage.getItem('sessionId'),
            'Content-Type': 'application/json'
        }
    });