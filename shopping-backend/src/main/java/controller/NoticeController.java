package controller;

import entity.Notice;
import service.NoticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import util.SessionManager;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/admin/announcement/*")
public class NoticeController extends HttpServlet {
    private final NoticeService noticeService = new NoticeService();
    private final ObjectMapper objectMapper = new ObjectMapper(); // 处理JSON
    private final SessionManager sessionManager = SessionManager.getInstance();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置编码和响应格式
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        // 跨域设置（与GoodsController保持一致）
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = req.getPathInfo();
        System.out.println("收到GET请求: " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(resp, "无效路径");
                return;
            }

            switch (pathInfo) {
                case "/list":
                    // 分页获取公告列表
                    int page = Integer.parseInt(req.getParameter("page") == null ? "1" : req.getParameter("page"));
                    int pageSize = Integer.parseInt(req.getParameter("pageSize") == null ? "10" : req.getParameter("pageSize"));
                    String keyword = decodeGetParam(req, "keyword"); // 处理GET中文参数
                    getNoticeList(page, pageSize, keyword, resp);
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 设置编码和响应格式
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        // 跨域设置
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = req.getPathInfo();
        System.out.println("收到POST请求: " + pathInfo);

        try {
            if (pathInfo == null) {
                sendError(resp, "无效路径");
                return;
            }

            switch (pathInfo) {
                case "/publish":
                    publishNotice(req, resp);
                    break;
                case "/edit":
                    editNotice(req, resp);
                    break;
                case "/delete":
                    // 错误修复：直接传递req和resp，而不是解析后的id
                    deleteNotice(req, resp);
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "服务器错误: " + e.getMessage());
        }
    }

    // ------------------- 业务方法 -------------------
    // 1. 获取公告列表（分页）
    private void getNoticeList(int page, int pageSize, String keyword, HttpServletResponse resp) throws IOException {
        try {
            Map<String, Object> result = noticeService.getNoticeList(page, pageSize, keyword);
            sendSuccess(resp, result);
        } catch (Exception e) {
            sendError(resp, "获取列表失败: " + e.getMessage());
        }
    }

    // 2. 发布公告
    private void publishNotice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 解析请求体为Notice对象
            Notice notice = objectMapper.readValue(req.getInputStream(), Notice.class);
            notice.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            notice.setUser("admin");
            // 调用Service
            noticeService.createNotice(notice);
            sendSuccess(resp, "发布成功");
        } catch (Exception e) {
            sendError(resp, "发布失败: " + e.getMessage());
        }
    }

    // 3. 编辑公告
    private void editNotice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Notice notice = objectMapper.readValue(req.getInputStream(), Notice.class);
            if (notice.getId() <= 0) {
                sendError(resp, "公告ID无效");
                return;
            }
            noticeService.updateNotice(notice);
            sendSuccess(resp, "编辑成功");
        } catch (Exception e) {
            sendError(resp, "编辑失败: " + e.getMessage());
        }
    }

    // 4. 删除公告
    private void deleteNotice(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 从JSON请求体中解析ID
            Map<String, Object> params = objectMapper.readValue(req.getInputStream(), Map.class);
            int id = (Integer) params.get("id");

            noticeService.deleteNotice(id);
            sendSuccess(resp, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            // 返回具体错误信息给前端
            sendError(resp, "删除失败: " + e.getMessage());
        }
    }

    // ------------------- 工具方法 -------------------
    // 解码GET参数（处理中文乱码）
    private String decodeGetParam(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        if (value == null) return null;
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    // 发送成功响应
    private void sendSuccess(HttpServletResponse resp, Object data) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        objectMapper.writeValue(resp.getWriter(), result);
    }

    // 发送错误响应
    private void sendError(HttpServletResponse resp, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        objectMapper.writeValue(resp.getWriter(), result);
    }
}