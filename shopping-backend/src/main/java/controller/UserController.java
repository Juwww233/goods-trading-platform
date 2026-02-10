package controller;

import service.UserService;
import entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/user/*")
public class UserController extends HttpServlet {
    static {
        System.out.println("======== UserController 已加载 ========");
    }

    private UserService userService = new UserService();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        // 跨域配置
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Session-Id");

        String pathInfo = req.getPathInfo();
        if ("/changePassword".equals(pathInfo)) {
            handleChangePassword(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // 处理修改密码逻辑
    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 验证会话
        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId == null) {
            sendError(resp, "未登录");
            return;
        }

        // 解析请求参数
        Map<String, String> params = objectMapper.readValue(req.getInputStream(), Map.class);
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        Integer userId = SessionManager.getInstance().getUserId(sessionId);

        // 参数校验
        if (userId == null || oldPassword == null || newPassword == null || newPassword.length() < 6) {
            sendError(resp, "参数错误，新密码长度不能少于6位");
            return;
        }

        // 调用服务层修改密码
        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (success) {
            sendSuccess(resp, "密码修改成功");
            // 密码修改后建议刷新会话（可选）
            //SessionManager.getInstance().refreshSession(sessionId);
        } else {
            sendError(resp, "原密码错误或修改失败");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        // 跨域配置（固定域名，允许携带Cookie）
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Session-Id");

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null) {
                sendError(resp, "无效路径");
                return;
            }
            switch (pathInfo) {
                case "/login":
                    handleLogin(req, resp);
                    break;
                case "/register":
                    handleRegister(req, resp);
                    break;
                case "/update":
                    handleUpdateUser(req, resp);
                    break;
                case "/uploadAvatar":
                    handleUploadAvatar(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "服务器错误: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        // 跨域配置（与POST保持一致）
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Session-Id");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 匹配 /user/{userId} 路径
        if (pathInfo.matches("/\\d+")) {
            String userIdStr = pathInfo.substring(1);
            try {
                int userId = Integer.parseInt(userIdStr);
                getUserInfo(userId, req, resp);
            } catch (NumberFormatException e) {
                sendError(resp, "无效的用户ID");
            }
        } else if ("/checkLogin".equals(pathInfo)) {
            checkLoginStatus(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // 获取用户信息
    private void getUserInfo(int userId, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 验证会话有效性
        String sessionId = req.getHeader("X-Session-Id");
        System.out.println("接收到的 sessionId: " + sessionId);
        System.out.println("用户 ID: " + userId);
        System.out.println("会话验证结果: " + SessionManager.getInstance().isValidSession(sessionId, userId));
        if (sessionId == null || !SessionManager.getInstance().isValidSession(sessionId, userId)) {
            sendError(resp, "会话无效，请重新登录");
            return;
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            sendError(resp, "用户不存在");
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUserName());
        userData.put("name", user.getName());
        userData.put("phone", user.getPhone());
        userData.put("role", user.getRole());
        userData.put("avatar", user.getAvatar());

        sendSuccess(resp, userData);
    }

    // 验证登录状态
    private void checkLoginStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String sessionId = req.getHeader("X-Session-Id");
        Map<String, Object> result = new HashMap<>();

        if (sessionId != null) {
            Integer userId = SessionManager.getInstance().getUserId(sessionId);
            if (userId != null) {
                User user = userService.getUserById(userId);
                if (user != null) {
                    result.put("success", true);
                    result.put("userId", user.getId());
                    result.put("username", user.getUserName());
                    resp.getWriter().write(objectMapper.writeValueAsString(result));
                    return;
                }
            }
        }

        result.put("success", false);
        result.put("message", "未登录");
        objectMapper.writeValue(resp.getWriter(), result);
    }

    // 登录逻辑
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User loginData = objectMapper.readValue(req.getInputStream(), User.class);
        String username = loginData.getUserName();
        String password = loginData.getPassword();

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            sendError(resp, "用户名和密码不能为空");
            return;
        }

        User user = userService.login(username, password);
        if (user != null) {
            // 创建会话
            String sessionId = SessionManager.getInstance().createSession(user.getId());
            System.out.println("登录成功，会话ID: " + sessionId);
            System.out.println("登录用户ID: " + user.getId());
            System.out.println("登录用户角色: " + user.getRole()); // 新增日志，确认角色


            // 返回会话ID和用户信息
            Map<String, Object> data = new HashMap<>();
            data.put("sessionId", sessionId);
            data.put("userId", user.getId());
            data.put("username", user.getUserName());
            data.put("role", user.getRole());
            sendSuccess(resp, data);
        } else {
            sendError(resp, "用户名或密码错误");
        }
    }

    // 注册逻辑
    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = objectMapper.readValue(req.getInputStream(), User.class);

        if (user.getUserName() == null || user.getPassword() == null ||
                user.getName() == null || user.getPhone() == null) {
            sendError(resp, "请完善注册信息");
            return;
        }

        boolean success = userService.register(user);
        if (success) {
            sendSuccess(resp, "注册成功，请登录");
        } else {
            sendError(resp, "注册失败，用户名可能已存在");
        }
    }

    // 更新用户信息
    private void handleUpdateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 从请求头获取会话ID
        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId == null) {
            sendError(resp, "未登录");
            return;
        }

        // 从请求体获取用户数据
        User updateData = objectMapper.readValue(req.getInputStream(), User.class);
        Integer userId = updateData.getId();

        // 验证会话和用户ID是否匹配
        if (!SessionManager.getInstance().isValidSession(sessionId, userId)) {
            sendError(resp, "会话无效或权限不足");
            return;
        }

        // 更新用户信息
        boolean success = userService.updateUser(updateData);
        if (success) {
            sendSuccess(resp, "更新成功");
        } else {
            sendError(resp, "更新失败");
        }
    }

    // 处理头像上传（简化版，实际需要处理文件上传）
    private void handleUploadAvatar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 注意：这里是简化示例，实际需要处理Multipart/form-data格式的文件上传
        // 推荐使用Apache Commons FileUpload或Servlet 3.0+的Part API

        // 从请求头获取会话ID
        String sessionId = req.getHeader("X-Session-Id");
        if (sessionId == null) {
            sendError(resp, "未登录");
            return;
        }

        // 从请求参数获取用户ID和头像URL（实际应从上传文件中获取）
        String userIdStr = req.getParameter("userId");
        String avatar = req.getParameter("avatar"); // 实际应该是上传后的文件路径

        if (userIdStr == null || avatar == null) {
            sendError(resp, "缺少必要参数");
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr);

            // 验证会话和用户ID是否匹配
            if (!SessionManager.getInstance().isValidSession(sessionId, userId)) {
                sendError(resp, "会话无效或权限不足");
                return;
            }

            // 更新用户头像
            boolean success = userService.updateAvatar(userId, avatar);
            if (success) {
                Map<String, Object> data = new HashMap<>();
                data.put("avatar", avatar);
                sendSuccess(resp, data);
            } else {
                sendError(resp, "头像更新失败");
            }
        } catch (NumberFormatException e) {
            sendError(resp, "无效的用户ID");
        }
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