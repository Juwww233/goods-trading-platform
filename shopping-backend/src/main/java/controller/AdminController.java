package controller;

import entity.Admin;
import entity.User;
import service.AdminService;
import service.GoodsService;
import service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/*")
public class AdminController extends HttpServlet {
    private UserService userService = new UserService();
    private AdminService adminService = new AdminService();
    private GoodsService goodsService = new GoodsService(); // 添加 GoodsService
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        String pathInfo = req.getPathInfo();
        if ("/info".equals(pathInfo)) {
            handleGetAdminInfo(req, resp);
        }
        // 处理获取用户列表接口（/admin/users）
        else if ("/users".equals(pathInfo)) {
            handleGetUsers(req, resp);
        }
        // 新增：处理获取商品列表接口（/admin/goods）
        else if ("/goods".equals(pathInfo)) {
            handleGetGoods(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            // 新增：处理商品审核通过接口（/admin/goods/{id}/approve）
            if (pathInfo.matches("/goods/\\d+/approve")) {
                int goodsId = Integer.parseInt(pathInfo.split("/")[2]);
                handleApproveGoods(goodsId, req, resp);
            }
            // 新增：处理商品拒绝接口（/admin/goods/{id}/reject）
            else if (pathInfo.matches("/goods/\\d+/reject")) {
                int goodsId = Integer.parseInt(pathInfo.split("/")[2]);
                handleRejectGoods(goodsId, req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");

        String pathInfo = req.getPathInfo();
        // 处理删除用户接口（/admin/users/{userId}）
        if (pathInfo != null && pathInfo.matches("/users/\\d+")) {
            String userIdStr = pathInfo.substring("/users/".length());
            try {
                int userId = Integer.parseInt(userIdStr);
                handleDeleteUser(userId, req, resp);
            } catch (NumberFormatException e) {
                sendError(resp, "无效的用户ID");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // 处理获取管理员信息的请求（保持不变）
    private void handleGetAdminInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId == null) {
            sendError(resp, "未登录");
            return;
        }

        Integer adminId = SessionManager.getInstance().getUserId(sessionId);
        if (adminId == null) {
            sendError(resp, "会话无效");
            return;
        }

        Admin admin = adminService.getAdminById(adminId);
        if (admin == null) {
            sendError(resp, "管理员不存在");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", admin.getId());
        data.put("username", admin.getUserName());
        data.put("name", admin.getName());
        data.put("role", admin.getRole());
        data.put("avatar", admin.getAvatar());
        data.put("phone", admin.getPhone());
        data.put("email", admin.getEmail());

        sendSuccess(resp, data);
    }

    // 获取用户列表（支持分页和搜索）
    private void handleGetUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. 验证管理员会话
        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId == null) {
            sendError(resp, "未登录");
            return;
        }

        Integer adminId = SessionManager.getInstance().getUserId(sessionId);
        if (adminId == null) {
            sendError(resp, "会话无效");
            return;
        }

        // 检查是否为管理员
        Admin admin = adminService.getAdminById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            sendError(resp, "权限不足");
            return;
        }

        // 2. 获取分页和搜索参数
        String keyword = req.getParameter("keyword") != null ? req.getParameter("keyword") : "";
        int page = Integer.parseInt(req.getParameter("page") != null ? req.getParameter("page") : "1");
        int pageSize = Integer.parseInt(req.getParameter("pageSize") != null ? req.getParameter("pageSize") : "10");

        // 3. 调用UserService获取用户列表
        java.util.Map<String, java.lang.Object> result = userService.getUsersByPage(keyword, page, pageSize);

        // 4. 返回结果
        sendSuccess(resp, result);
    }

    // 删除用户
    private void handleDeleteUser(int userId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. 验证管理员会话
        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId == null) {
            sendError(resp, "未登录");
            return;
        }

        Integer adminId = SessionManager.getInstance().getUserId(sessionId);
        if (adminId == null) {
            sendError(resp, "会话无效");
            return;
        }

        // 检查是否为管理员
        Admin admin = adminService.getAdminById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            sendError(resp, "权限不足");
            return;
        }

        // 2. 调用UserService删除用户
        boolean success = userService.deleteUserById(userId);
        if (success) {
            sendSuccess(resp, "删除成功");
        } else {
            sendError(resp, "删除失败，用户不存在");
        }
    }

    // 新增：处理获取商品列表的方法
    private void handleGetGoods(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 验证管理员权限
        if (!validateAdminSession(req, resp)) {
            return;
        }

        try {
            // 获取查询参数
            String keyword = req.getParameter("keyword");
            String status = req.getParameter("status");
            int page = Integer.parseInt(req.getParameter("page"));
            int pageSize = Integer.parseInt(req.getParameter("pageSize"));

            // 调用 GoodsService 获取商品列表
            Map<String, Object> result = goodsService.getAdminGoodsList(keyword, status, page, pageSize);

            sendSuccess(resp, result);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "获取商品列表失败: " + e.getMessage());
        }
    }

    // 新增：处理商品审核通过的方法
    private void handleApproveGoods(int goodsId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 验证管理员权限
        if (!validateAdminSession(req, resp)) {
            return;
        }

        try {
            boolean success = goodsService.approveGoods(goodsId);
            if (success) {
                sendSuccess(resp, "商品审核通过");
            } else {
                sendError(resp, "商品审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "商品审核出错: " + e.getMessage());
        }
    }

    // 新增：处理商品拒绝的方法
    private void handleRejectGoods(int goodsId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 验证管理员权限
        if (!validateAdminSession(req, resp)) {
            return;
        }

        try {
            // 读取拒绝原因
            Map<String, String> requestData = objectMapper.readValue(req.getInputStream(), Map.class);
            String reason = requestData.get("reason");

            boolean success = goodsService.rejectGoods(goodsId, reason);
            if (success) {
                sendSuccess(resp, "商品已拒绝");
            } else {
                sendError(resp, "拒绝商品失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "拒绝商品出错: " + e.getMessage());
        }
    }

    // 辅助方法：验证管理员会话
    private boolean validateAdminSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId == null) {
            sendError(resp, "未登录");
            return false;
        }

        Integer adminId = SessionManager.getInstance().getUserId(sessionId);
        if (adminId == null) {
            sendError(resp, "会话无效");
            return false;
        }

        Admin admin = adminService.getAdminById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            sendError(resp, "权限不足");
            return false;
        }

        return true;
    }


    // 响应工具方法
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