package controller;

import service.CommentService;
import dao.OrdersDao;
import entity.Comment;
import entity.Orders;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.session.SqlSession;
import util.MyBatisUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet("/comments/*")
public class CommentController extends HttpServlet {
    private CommentService commentService = new CommentService();
    private OrdersDao ordersDao;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() {
        SqlSession sqlSession = MyBatisUtil.getSession();
        ordersDao = sqlSession.getMapper(OrdersDao.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 保持不变
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            sendError(resp, "无效路径");
            return;
        }

        try {
            if (pathInfo.equals("/my")) {
                String userIdStr = req.getParameter("userId");
                if (userIdStr == null || userIdStr.isEmpty()) {
                    sendError(resp, "用户ID不能为空");
                    return;
                }
                Integer userId = Integer.parseInt(userIdStr);
                List<Map<String, Object>> myComments = commentService.getMyComments(userId);
                sendSuccess(resp, myComments);
            } else if (pathInfo.startsWith("/goods/")) {
                String[] parts = pathInfo.split("/");
                if (parts.length < 3) {
                    sendError(resp, "商品ID缺失");
                    return;
                }
                Integer goodsId = Integer.parseInt(parts[2]);
                Integer page = Integer.parseInt(req.getParameter("page") == null ? "1" : req.getParameter("page"));
                Integer pageSize = 5;

                Map<String, Object> result = commentService.getCommentsByGoodsId(goodsId, page, pageSize);
                sendSuccess(resp, result);
            } else {
                resp.sendError(404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "服务器错误：" + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            sendError(resp, "无效路径");
            return;
        }

        try {
            if (pathInfo.equals("/delete")) {
                Map<String, Object> params = objectMapper.readValue(req.getInputStream(), Map.class);
                Integer commentId = (Integer) params.get("id");
                if (commentId == null) {
                    sendError(resp, "评论ID不能为空");
                    return;
                }
                boolean success = commentService.deleteComment(commentId);
                if (success) {
                    sendSuccess(resp, "删除成功");
                } else {
                    sendError(resp, "删除失败");
                }
            }
            // 修复评论权限检查的userId类型转换
            else if (pathInfo.equals("/checkPermission")) {
                Map<String, Object> params = objectMapper.readValue(req.getInputStream(), Map.class);

                // 关键修复：将String解析为Integer，而非直接强转
                String userIdStr = (String) params.get("userId");
                if (userIdStr == null || userIdStr.isEmpty()) {
                    sendError(resp, "用户ID不能为空");
                    return;
                }
                Integer userId = Integer.parseInt(userIdStr); // 安全解析

                String goodsName = (String) params.get("goodsName");
                if (goodsName == null || goodsName.isEmpty()) {
                    sendError(resp, "商品名称不能为空");
                    return;
                }

                // 关键修改：使用新的SqlSession查询订单，避免缓存影响
                List<Orders> userOrders = null;
                try (SqlSession newSession = MyBatisUtil.getSession()) {
                    OrdersDao newOrdersDao = newSession.getMapper(OrdersDao.class);
                    userOrders = newOrdersDao.getOrdersByUserId(userId);
                }

                boolean hasReceived = false;
                boolean hasPurchased = false;

                for (Orders order : userOrders) {
                    if (goodsName.equals(order.getGoodsName())) {
                        hasPurchased = true;
                        if ("已收货".equals(order.getStatus())) {
                            hasReceived = true;
                            break;
                        }
                    }
                }

                if (hasReceived) {
                    sendSuccess(resp, true);
                } else if (hasPurchased) {
                    sendError(resp, "收货后才可以评论哦");
                } else {
                    sendError(resp, "购买该商品并收货后才能评论哦");
                }
            } else if (pathInfo.equals("/add")) {
                Comment comment = objectMapper.readValue(req.getInputStream(), Comment.class);
                comment.setTime(new Date().toLocaleString());
                boolean success = commentService.addComment(comment);
                if (success) {
                    sendSuccess(resp, "评论成功");
                } else {
                    sendError(resp, "评论失败");
                }
            } else {
                resp.sendError(404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "服务器错误：" + e.getMessage());
        }
    }

    private void sendSuccess(HttpServletResponse resp, Object data) throws IOException {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("data", data);
        resp.getWriter().write(objectMapper.writeValueAsString(res));
    }

    private void sendError(HttpServletResponse resp, String msg) throws IOException {
        Map<String, Object> res = new HashMap<>();
        res.put("success", false);
        res.put("message", msg);
        resp.getWriter().write(objectMapper.writeValueAsString(res));
    }
}
