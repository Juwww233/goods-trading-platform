// src/api/collect.js（修正后）
import axios from './index'; // 导入配置好的axios实例（包含基础路径、跨域等配置）

// 1. 获取购物车商品列表（传递userId）
export const getCartItems = (userId) => {
    // 修正：使用导入的axios实例，而非未定义的instance
    return axios.get('/collect', {
        params: { userId: userId } // 传递用户ID作为查询参数
    });
};

// 2. 添加商品到购物车
export const addToCart = (params) => {
    console.log('[DEBUG] 添加购物车参数:', params);
    return axios.post('/collect', null, {
        params: { userId: params.userId, goodsId: params.goodsId }
    });
};

// 3. 从购物车移除商品（传递userId和goodsId）
export const removeFromCart = (goodsId, userId) => {
    // 修正：删除商品时需传递userId（与后端接口匹配）
    return axios.delete('/collect', {
        params: {
            goodsId: goodsId,
            userId: userId // 后端需要userId验证权限
        }
    });
};