package controller;

import service.OrdersService;
import entity.Orders;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/orders/*")
public class OrdersController extends HttpServlet {
    private OrdersService orderService = new OrdersService();
    private ObjectMapper objectMapper = new ObjectMapper();

    private void getOrdersBySaleId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 获取前端传递的saleId参数
            Integer saleId = Integer.parseInt(req.getParameter("saleId"));
            if (saleId == null || saleId <= 0) {
                sendError(resp, "无效的商家ID");
                return;
            }
            // 调用Service层新方法
            List<Orders> orders = orderService.getOrdersBySaleId(saleId);
            sendSuccess(resp, orders);
        } catch (NumberFormatException e) {
            sendError(resp, "无效的商家ID格式");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "查询商家订单失败：" + e.getMessage());
        }
    }

    private void receiveOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer orderId = Integer.parseInt(req.getParameter("orderId"));
            if (orderId == null || orderId <= 0) {
                sendError(resp, "无效的订单ID");
                return;
            }

            // 调用Service层确认收货（更新状态为“已收货”）
            boolean success = orderService.receiveOrder(orderId);
            if (success) {
                sendSuccess(resp, "确认收货成功");
            } else {
                sendError(resp, "确认收货失败（订单状态异常或不存在）");
            }
        } catch (NumberFormatException e) {
            sendError(resp, "无效的订单ID");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "服务器错误：" + e.getMessage());
        }
    }



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(resp, "无效路径");
                return;
            }

            switch (pathInfo) {
                case "/detail":
                    getOrderDetail(req, resp);
                    break;
                case "/user":
                    getUserOrders(req, resp);
                    break;
                case "/sale":  // 新增：处理商家订单查询
                    getOrdersBySaleId(req, resp);
                    break;
                case "/byName":  // 新增：处理商品名称查询订单
                    getOrdersByName(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            sendError(resp, "服务器错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null) {
                sendError(resp, "无效路径");
                return;
            }

            switch (pathInfo) {
                case "/create":
                    createOrder(req, resp);
                    break;
                case "/pay":
                    payOrder(req, resp);
                    break;
                case "/cancel":
                    cancelOrder(req, resp);
                    break;
                case "/receive":  // 新增：处理确认收货
                    receiveOrder(req, resp);
                    break;
                case "/updateStatus": // 新增：处理订单状态更新（发货操作）
                    updateOrderStatus(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            sendError(resp, "请求处理失败: " + e.getMessage());
        }
    }

    private void getOrdersByName(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 获取前端传递的商品名称参数
            String goodsName = req.getParameter("goodsName");
            if (goodsName == null || goodsName.trim().isEmpty()) {
                sendError(resp, "商品名称不能为空");
                return;
            }

            // 调用Service层方法查询订单
            List<Orders> orders = orderService.getOrdersByName(goodsName);
            sendSuccess(resp, orders);  // 返回查询结果
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "查询订单失败: " + e.getMessage());
        }
    }

    private void updateOrderStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 解析前端传递的订单ID和目标状态
            Orders order = objectMapper.readValue(req.getInputStream(), Orders.class);
            Integer orderId = order.getId();
            String status = order.getStatus();

            if (orderId == null || status == null) {
                sendError(resp, "订单ID和状态不能为空");
                return;
            }

            // 调用Service层更新状态（发货时将状态改为“已发货”）
            boolean success = orderService.updateOrderStatus(orderId, status, null, null);
            if (success) {
                sendSuccess(resp, "订单状态更新成功");
            } else {
                sendError(resp, "更新失败（订单不存在或状态异常）");
            }
        } catch (Exception e) {
            sendError(resp, "更新订单状态失败: " + e.getMessage());
        }
    }

    // 获取订单详情
    private void getOrderDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer orderId = Integer.parseInt(req.getParameter("orderId"));
            Orders order = orderService.getOrderDetail(orderId);
            if (order == null) {
                sendError(resp, "订单不存在");
                return;
            }
            sendSuccess(resp, order);
        } catch (NumberFormatException e) {
            sendError(resp, "无效的订单ID");
        }
    }

    // 获取用户订单列表
    private void getUserOrders(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer userId = Integer.parseInt(req.getParameter("userId"));
            List<Orders> orders = orderService.getUserOrders(userId);
            sendSuccess(resp, orders);
        } catch (NumberFormatException e) {
            sendError(resp, "无效的用户ID");
        }
    }

    // 创建订单（核心修改）
    private void createOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 读取前端JSON并转换为Orders对象（使用新实体类）
            Orders order = objectMapper.readValue(req.getInputStream(), Orders.class);

            // 基础验证（适配新字段）
            if (order.getTotal() == null || order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
                sendError(resp, "订单总金额必须大于0");
                return;
            }
            if (order.getAddress() == null || order.getAddress().isEmpty()) {
                sendError(resp, "收货地址不能为空");
                return;
            }
            // 新增商品信息验证
            if (order.getGoodsName() == null || order.getGoodsName().isEmpty()) {
                sendError(resp, "商品名称不能为空");
                return;
            }
            if (order.getCount() == null || order.getCount() <= 0) {
                sendError(resp, "购买数量必须大于0");
                return;
            }

            boolean success = orderService.createOrder(order);
            if (success) {
                sendSuccess(resp, order); // 返回包含订单ID的完整数据
            } else {
                sendError(resp, "订单创建失败");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细错误日志
            sendError(resp, "参数错误: " + e.getMessage());
        }
    }

    // 支付订单（适配新字段）
    private void payOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer orderId = Integer.parseInt(req.getParameter("orderId"));
            String payNo = req.getParameter("payNo");
            // 新增支付时间参数（前端需传递）
            String payTime = req.getParameter("payTime");

            boolean success = orderService.payOrder(orderId, payNo, payTime);
            if (success) {
                sendSuccess(resp, "订单支付成功");
            } else {
                sendError(resp, "订单支付失败");
            }
        } catch (NumberFormatException e) {
            sendError(resp, "无效的订单ID");
        }
    }

    // 取消订单
    private void cancelOrder(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Integer orderId = Integer.parseInt(req.getParameter("orderId"));
            boolean success = orderService.cancelOrder(orderId);
            if (success) {
                sendSuccess(resp, "订单已取消");
            } else {
                sendError(resp, "取消失败");
            }
        } catch (NumberFormatException e) {
            sendError(resp, "无效的订单ID");
        }
    }

    // 响应工具方法（保持不变）
    private void sendSuccess(HttpServletResponse resp, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        objectMapper.writeValue(resp.getWriter(), result);
    }

    private void sendError(HttpServletResponse resp, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        objectMapper.writeValue(resp.getWriter(), result);
    }
}