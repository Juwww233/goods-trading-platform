package dao;

import entity.Orders;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;

public interface OrdersDao {

    List<Orders> getOrdersByName(@Param("goodsName") String goodsName);
    List<Orders> getOrdersBySaleId(@Param("saleId") Integer saleId);
    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态（如"已收货"）
     * @param payNo 支付单号（可为null）
     * @param payTime 支付时间（可为null）
     * @return 影响行数
     */
    int updateOrderStatus(
            @Param("orderId") Integer orderId,  // 对应XML中的#{orderId}
            @Param("status") String status,      // 对应XML中的#{status}
            @Param("payNo") String payNo,        // 对应XML中的#{payNo}
            @Param("payTime") String payTime     // 对应XML中的#{payTime}
    );

    // 1. 根据ID查询订单详情
    Orders getOrderById(Integer id);

    // 2. 查询用户订单列表
    List<Orders> getOrdersByUserId(Integer userId);

    // 3. 创建订单（整合原insertOrder和insertOrderItem）
    int insertOrder(Orders order);

    // 4. 更新订单状态
    //int updateOrderStatus(Integer orderId, String status, String payNo, String payTime);

    // 5. 生成唯一订单号
    String generateOrderNo();

    // 6. 删除订单（逻辑删除）
    int deleteOrder(Integer orderId);

    // 7. 查询待支付订单（超时处理）
    List<Orders> getPendingOrders(long expireTime);

    // 8. 根据订单号查询订单
    Orders getOrderByOrderNo(String orderNo);
}