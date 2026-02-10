// orders.js
import api from './index'; // 导入配置好的axios实例（命名为api）

// 新增：通过商品名称查询订单
export const getOrdersByName = (goodsName) => {
    return api.get('/orders/byName', {
        params: { goodsName: goodsName }  // 传递商品名称参数
    });
};

// 新增：定义更新订单状态的API（用于发货操作）
export const updateOrderStatus = (orderId, status) => {
    return api.post('/orders/updateStatus', {
        id: orderId,
        status: status
    });
};

export const getOrdersBySaleId = (saleId) => {
    return api.get('/orders/sale', { params: { saleId } });
};

// 获取用户订单列表
export const getUserOrders = (userId) => {
    return api.get('/orders/user', { // 使用api而不是axios
        params: { userId }
    });
};

export const confirmReceipt = (orderId) => {
    return api.post('/orders/receive', null, {
        params: { orderId }
    });
};

// 获取订单详情
export const getOrderById = (orderId) => {
    return api.get('/orders/detail', { // 使用api而不是axios
        params: { orderId }
    });
};

// 以下为保持一致的其他API方法
export const getOrdersByUser = (userId) =>
    api.get('/orders/user', { params: { userId } });

/**
 * 获取订单详情
 * @param {number} orderId - 订单ID
 * @returns {Promise} - 订单详情数据
 */
export const getOrderDetail = (orderId) =>
    api.get('/orders/detail', { params: { orderId } });

/**
 * 创建订单
 * @param {Object} orderData - 订单数据（与后端实体类字段对应）
 * @param {string} orderData.goodsName - 商品名称（必填）
 * @param {string} orderData.goodsImg - 商品图片（必填）
 * @param {number} orderData.goodsPrice - 商品单价（必填，保留两位小数）
 * @param {number} orderData.count - 购买数量（必填，>0）
 * @param {number} orderData.total - 订单总价（必填，可前端计算或后端生成）
 * @param {string} orderData.address - 收货地址（必填）
 * @param {string} orderData.phone - 联系电话（必填）
 * @param {string} orderData.userName - 收货人姓名（必填）
 * @param {number} orderData.userId - 下单用户ID（必填）
 * @param {number} orderData.saleId - 卖家ID（必填）
 * @returns {Promise} - 创建成功的订单数据（含orderNo和id）
 */
export const createOrder = (orderData) =>
    api.post('/orders/create', orderData);

/**
 * 支付订单
 * @param {Object} payData - 支付数据
 * @param {number} payData.orderId - 订单ID（必填）
 * @param {string} payData.payNo - 支付单号（必填，如第三方支付流水号）
 * @param {string} [payData.payTime] - 支付时间（可选，后端会自动生成）
 * @returns {Promise} - 支付结果
 */
export const payOrder = (payData) =>
    api.post('/orders/pay', null, {
        params: {
            orderId: payData.orderId,
            payNo: payData.payNo,
            payTime: payData.payTime
        }
    });

/**
 * 取消订单
 * @param {number} orderId - 订单ID
 * @returns {Promise} - 取消结果
 */
export const cancelOrder = (orderId) =>
    api.post('/orders/cancel', null, { params: { orderId } });

/**
 * 订单状态映射（与后端Orders类的静态常量保持一致）
 */
export const OrderStatus = {
    PENDING: '待支付',
    PAID: '已支付',
    SHIPPED: '已发货',
    COMPLETED: '已完成',
    CANCELLED: '已取消'
};
