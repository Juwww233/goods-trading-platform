import api from './index';

export const deleteGoods = (goodsId) => {
    return api.delete(`/goods/delete`, {
        params: { id: goodsId } // 传递商品ID作为参数
    });
};

export const updateGoods = (goodsData) => {
    return api.put('/goods/update', goodsData);
};

export const getGoodsByUserId = (userId) => {
    // 对应后端Controller支持的路径（二选一即可）
    // 方式1：路径参数
    return api.get(`/goods/user/${userId}`);
    // 方式2：请求参数
    // return api.get('/goods/user', { params: { userId } });
};

export const getGoodsIdByName = (goodsName) => {
    return api.get('/goods/getIdByName', {
        params: { goodsName }
    });
};

// 获取商品广场数据
export const getGoodsPlaza = () =>
    api.get('/goods/plaza');

// 获取商品详情
export const getGoodsDetail = (goodsId, isManage = false) => {
    return api.get('/goods/detail', {
        params: {
            id: goodsId,
            isManage: isManage  // 管理页面调用时传递true
        }
    });
};

// 使用查询参数而非路径参数
export const getGoodsByCategory = (category) =>
    api.get('/goods/category', { params: { category } });

// 搜索商品
export const searchGoods = (keyword) =>
    api.get('/goods/search', { params: { keyword } });

// 获取猜你喜欢商品
export const getGuessYouLike = () =>
    api.get('/goods/guessYouLike');

// 获取二手物品
export const getSecondHandGoods = () =>
    api.get('/goods/secondHand');

// 发布商品
export const publishGoods = (data) =>
    api.post('/goods/publish', data);

// 更新商品上架状态（使用PUT更符合RESTful）
export const updateSaleStatus = (id, saleStatus) =>
    api.put('/goods/status', { id, saleStatus });

//添加购物车
export const addToCart = (data) =>
    api.post('/cart/add', data);