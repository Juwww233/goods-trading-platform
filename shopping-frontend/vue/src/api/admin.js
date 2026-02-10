// 修正导入路径，使用项目中实际的axios实例路径
import api from './index';  // 改为与登录接口一致的导入方式（参考之前的user.js）

// 添加获取管理员信息的接口
export const getAdminInfo = () => {
    return api.get('/admin/info'); // 后端需提供对应的接口
};


// 用户管理
export const getUsers = (params) => {
    return api.get('/admin/users', { params });
};

export const deleteUser = (userId) => {
    return api.delete(`/admin/users/${userId}`);
};

// 商品管理
export const getGoods = (params) => {
    return api.get('/admin/goods', { params });
};

export const approveGoods = (goodsId) => {
    return api.post(`/admin/goods/${goodsId}/approve`);
};

export const rejectGoods = (goodsId, reason) => {
    return api.post(`/admin/goods/${goodsId}/reject`, { reason });
};

// 公告管理
// 获取公告列表（分页）
export const getAnnouncements = (params) => {
    return api.get('/admin/announcement/list', { params });
};

// 发布公告
export const createAnnouncement = (data) => {
    return api.post('/admin/announcement/publish', data);
};

// 编辑公告
export const updateAnnouncement = (data) => {
    return api.post('/admin/announcement/edit', data);
};

// 删除公告
export const deleteAnnouncement = (id) => {
    return api.post('/admin/announcement/delete', { id });
};