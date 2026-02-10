package service;

import dao.OrdersDao;
import entity.Orders;
import util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OrdersService {

    public List<Orders> getOrdersBySaleId(Integer saleId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            OrdersDao dao = session.getMapper(OrdersDao.class);
            // 调用DAO层新方法
            return dao.getOrdersBySaleId(saleId);
        } catch (Exception e) {
            throw new RuntimeException("根据商家ID查询订单失败：" + e.getMessage(), e);
        }
    }

    public boolean receiveOrder(Integer orderId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            OrdersDao dao = session.getMapper(OrdersDao.class);

            // 1. 先查询订单，验证状态是否为“已发货”
            Orders order = dao.getOrderById(orderId);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            if (!Orders.STATUS_SHIPPED.equals(order.getStatus())) {
                // 订单状态不是“已发货”，不允许确认收货
                throw new RuntimeException("只能对已发货的订单确认收货");
            }

            // 2. 更新订单状态为“已收货”
            int result = dao.updateOrderStatus(
                    orderId,
                    Orders.STATUS_RECEIVED,  // 状态改为“已收货”
                    null,  // payNo不变
                    null   // payTime不变
            );
            session.commit();
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("确认收货失败：" + e.getMessage(), e);
        }
    }

    // 1. 创建订单（适配新表结构）
    public boolean createOrder(Orders order) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            OrdersDao dao = session.getMapper(OrdersDao.class);

            // 设置默认值（匹配新表字段）
            order.setStatus(Orders.STATUS_PAID); // 已支付
            order.setOrderNo(generateOrderNo());    // 生成订单号
            order.setTime(formatCurrentTime());     // 下单时间（String类型）

            // 计算总价（如果前端未传递，可根据商品单价和数量计算）
            if (order.getTotal() == null) {
                BigDecimal total = order.getGoodsPrice()
                        .multiply(new BigDecimal(order.getCount()));
                order.setTotal(total);
            }
            System.out.println("即将插入数据库的订单数据：" + order.toString());
            // 插入订单（新表结构无需关联商品表）
            int result = dao.insertOrder(order);
            session.commit();
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("创建订单失败", e);
        }
    }

    // 生成唯一订单号
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }

    // 格式化当前时间为字符串（匹配数据库VARCHAR类型）
    private String formatCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    // 2. 获取订单详情（无需关联商品列表）
    public Orders getOrderDetail(Integer orderId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            OrdersDao dao = session.getMapper(OrdersDao.class);
            return dao.getOrderById(orderId);
        }
    }

    // 3. 获取用户订单列表
    public List<Orders> getUserOrders(Integer userId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            OrdersDao dao = session.getMapper(OrdersDao.class);
            return dao.getOrdersByUserId(userId);
        }
    }

    // 4. 更新订单状态（适配新表字段）
    public boolean updateOrderStatus(Integer orderId, String status, String payNo, String payTime) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            OrdersDao dao = session.getMapper(OrdersDao.class);
            int result = dao.updateOrderStatus(orderId, status, payNo, payTime);
            session.commit();
            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新订单状态失败", e);
        }
    }

    // 5. 取消订单（状态改为已取消）
    public boolean cancelOrder(Integer orderId) {
        return updateOrderStatus(orderId, Orders.STATUS_CANCELLED, null, null);
    }

    // 6. 支付订单（更新支付信息）
    public boolean payOrder(Integer orderId, String payNo, String payTime) {
        // 如果前端未传递支付时间，使用当前时间
        if (payTime == null) {
            payTime = formatCurrentTime();
        }
        return updateOrderStatus(orderId, Orders.STATUS_PAID, payNo, payTime);
    }

    // 7. 查询待支付订单（超时处理）
    public List<Orders> getPendingOrders(long expireTime) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            OrdersDao dao = session.getMapper(OrdersDao.class);
            return dao.getPendingOrders(expireTime);
        }
    }

    // OrdersService.java
    public List<Orders> getOrdersByName(String goodsName) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            OrdersDao dao = session.getMapper(OrdersDao.class);
            // 调用DAO层通过商品名称查询订单的方法
            return dao.getOrdersByName(goodsName);
        } catch (Exception e) {
            throw new RuntimeException("根据商品名称查询订单失败：" + e.getMessage(), e);
        }
    }
}