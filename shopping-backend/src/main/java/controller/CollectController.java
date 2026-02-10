package controller;

import entity.Collect;
import entity.Goods;
import service.CollectService;
import service.GoodsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/collect/*")
public class CollectController extends HttpServlet {
    private final CollectService collectService = new CollectService();
    private final GoodsService goodsService = new GoodsService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // CollectController.java
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        PrintWriter out = resp.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取前端传递的userId和goodsId（从查询参数中获取）
            String userIdStr = req.getParameter("userId");
            String goodsIdStr = req.getParameter("goodsId");

            // 验证参数
            if (userIdStr == null || goodsIdStr == null) {
                result.put("success", false);
                result.put("message", "用户ID和商品ID不能为空");
                out.write(objectMapper.writeValueAsString(result));
                return;
            }

            Integer userId = Integer.parseInt(userIdStr);
            Integer goodsId = Integer.parseInt(goodsIdStr);

            // 调用Service层添加到购物车
            boolean success = collectService.add(userId, goodsId);
            if (success) {
                result.put("success", true);
                result.put("message", "添加到购物车成功");
            } else {
                result.put("success", false);
                result.put("message", "该商品已在购物车中");
            }
            out.write(objectMapper.writeValueAsString(result));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的ID格式");
            out.write(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "添加到购物车失败：" + e.getMessage());
            out.write(objectMapper.writeValueAsString(result));
        } finally {
            out.close();
        }
    }

    // 获取购物车商品列表（从前端接收userId）
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        // 允许跨域请求（与前端地址匹配）
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true"); // 允许携带cookie
        PrintWriter out = resp.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            // 关键修改：从请求参数获取userId（不再硬编码）
            String userIdStr = req.getParameter("userId");
            if (userIdStr == null || userIdStr.isEmpty()) {
                result.put("success", false);
                result.put("message", "用户ID不能为空");
                out.write(objectMapper.writeValueAsString(result));
                return;
            }
            Integer userId = Integer.parseInt(userIdStr);

            // 查询该用户的购物车商品
            List<Collect> collectList = collectService.getByUserId(userId);
            List<Map<String, Object>> cartItems = new ArrayList<>();

            for (Collect collect : collectList) {
                Goods goods = goodsService.getGoodsById(collect.getGoodsId());
                if (goods != null) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", goods.getId());
                    item.put("name", goods.getName());
                    item.put("img", goods.getImg());
                    item.put("price", goods.getPrice());
                    item.put("description", goods.getContent());
                    item.put("address", goods.getAddress());
                    item.put("saleId", goods.getUserId());
                    cartItems.add(item);
                }
            }

            result.put("success", true);
            result.put("data", cartItems);
            out.write(objectMapper.writeValueAsString(result));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的用户ID");
            out.write(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取购物车失败：" + e.getMessage());
            out.write(objectMapper.writeValueAsString(result));
        } finally {
            out.close();
        }
    }

    // 从购物车移除商品（从前端接收userId和goodsId）
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        PrintWriter out = resp.getWriter();
        Map<String, Object> result = new HashMap<>();

        try {
            // 关键修改：从请求参数获取userId和goodsId
            String userIdStr = req.getParameter("userId");
            String goodsIdStr = req.getParameter("goodsId");
            if (userIdStr == null || goodsIdStr == null) {
                result.put("success", false);
                result.put("message", "用户ID和商品ID不能为空");
                out.write(objectMapper.writeValueAsString(result));
                return;
            }
            Integer userId = Integer.parseInt(userIdStr);
            Integer goodsId = Integer.parseInt(goodsIdStr);

            // 调用服务层删除该用户的商品
            boolean success = collectService.remove(userId, goodsId);
            if (success) {
                result.put("success", true);
                result.put("message", "移除成功");
            } else {
                result.put("success", false);
                result.put("message", "移除失败（商品不存在或权限不足）");
            }
            out.write(objectMapper.writeValueAsString(result));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "无效的ID格式");
            out.write(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "移除失败：" + e.getMessage());
            out.write(objectMapper.writeValueAsString(result));
        } finally {
            out.close();
        }
    }

    // 处理预检请求（跨域时浏览器自动发送）
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}