import api from './index';

export const getMyComments = (userId) => {
    return api.get('/comments/my', { params: { userId } });
};

// 删除评论
export const deleteComment = (commentId) => {
    return api.post('/comments/delete', { id: commentId });
};
// 获取商品评论（分页）
export const getGoodsComments = (goodsId, page) => {
    return api.get(`/comments/goods/${goodsId}`, { params: { page } });
};

// 检查评论权限
export const checkCommentPermission = (userId, goodsName) => {
    return api.post('/comments/checkPermission', { userId, goodsName });
};

// 提交评论
export const submitComment = (comment) => {
    return api.post('/comments/add', comment);
};