package controller;

import service.GoodsService;
import entity.Goods;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/goods/*")
public class GoodsController extends HttpServlet {
    static {
        System.out.println("======== GoodsController 已加载 ========");
    }

    private GoodsService goodsService = new GoodsService();
    private ObjectMapper objectMapper = new ObjectMapper(); // JSON 转换工具

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = req.getPathInfo();
        try {
            // 验证路径（仅处理/goods/delete请求）
            if (pathInfo == null || !pathInfo.equals("/delete")) {
                sendError(resp, "无效路径");
                return;
            }

            // 获取商品ID参数
            String goodsIdStr = req.getParameter("id");
            if (goodsIdStr == null || goodsIdStr.isEmpty()) {
                sendError(resp, "商品ID不能为空");
                return;
            }
            int goodsId = Integer.parseInt(goodsIdStr);

            // 从请求头获取全局会话ID，验证登录状态
            String sessionId = req.getHeader("X-Session-Id");
            Integer userId = util.SessionManager.getInstance().getUserId(sessionId);
            if (sessionId == null || userId == null) {
                sendError(resp, "请先登录");
                return;
            }

            // 调用Service层删除商品
            boolean success = goodsService.deleteGoods(goodsId, userId);
            if (success) {
                sendSuccess(resp, "商品删除成功");
            } else {
                sendError(resp, "删除失败（权限不足或商品不存在）");
            }
        } catch (NumberFormatException e) {
            sendError(resp, "无效的商品ID");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "删除失败：" + e.getMessage());
        }
    }


    private void getGoodsByUserId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 获取用户ID参数（支持路径参数或请求参数）
            String userIdStr = req.getParameter("userId");
            if (userIdStr == null || userIdStr.isEmpty()) {
                // 尝试从路径获取（如 /goods/user/4）
                String pathInfo = req.getPathInfo();
                String[] parts = pathInfo.split("/");
                if (parts.length >= 3) {
                    userIdStr = parts[2];
                } else {
                    sendError(resp, "用户ID不能为空");
                    return;
                }
            }

            int userId = Integer.parseInt(userIdStr);
            List<Goods> goodsList = goodsService.getGoodsByUserId(userId);
            sendSuccess(resp, goodsList);
        } catch (NumberFormatException e) {
            sendError(resp, "无效的用户ID");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "查询失败：" + e.getMessage());
        }
    }

    private void getIdByName(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String goodsName = req.getParameter("goodsName");
            if (goodsName == null || goodsName.trim().isEmpty()) {
                sendError(resp, "商品名称不能为空");
                return;
            }

            // 调用Service查询商品ID
            Integer goodsId = goodsService.getIdByName(goodsName);
            if (goodsId != null) {
                sendSuccess(resp, goodsId); // 返回商品ID
            } else {
                sendError(resp, "未找到该商品");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "查询失败：" + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 强制设置请求编码（对 POST 表单有效，GET URL 参数需手动解码）
        req.setCharacterEncoding("UTF-8");
        // 2. 设置响应类型及跨域头
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = req.getPathInfo();
        System.out.println("收到请求: " + req.getMethod() + " " + pathInfo);

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendError(resp, "无效路径");
                return;
            }

            // 统一处理请求路径
            switch (pathInfo) {
                case "/user":
                case "/user/":
                    getGoodsByUserId(req, resp); // 处理 /goods/user?userId=4
                    break;
                case "/plaza":
                    getOnSaleGoods(resp);
                    break;
                case "/detail":
                    getGoodsDetail(req, resp);
                    break;
                case "/category":
                    // 修复 GET 请求中文参数乱码：手动解码
                    String category = decodeGetParam(req, "category");
                    if (category == null || category.isEmpty()) {
                        sendError(resp, "分类参数不能为空");
                        return;
                    }
                    System.out.println("获取分类商品: " + category);
                    getGoodsByCategory(category, resp); // 直接调用优化后的方法
                    break;
                case "/search":
                    String keyword = decodeGetParam(req, "keyword");
                    if (keyword == null || keyword.isEmpty()) {
                        sendError(resp, "搜索关键词不能为空");
                        return;
                    }
                    searchGoods(keyword, resp);
                    break;
                case "/guessYouLike":
                    System.out.println("处理 /guessYouLike 请求");
                    getGuessYouLike(resp);
                    break;
                case "/secondHand":
                    getSecondHandGoods(resp);
                    break;

                case "/getIdByName":  // 没有这个case分支
                    getIdByName(req, resp);
                    break;

                default:
                    //resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    if (pathInfo.startsWith("/user/")) {
                        getGoodsByUserId(req, resp);
                    } else {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "服务器内部错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. 设置请求编码（POST 表单参数有效）
        req.setCharacterEncoding("UTF-8");
        // 2. 设置响应类型及跨域头
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            sendError(resp, "无效路径");
            return;
        }

        switch (pathInfo) {
            case "/publish":
                publishGoods(req, resp);
                break;
            case "/updateSaleStatus":
                updateSaleStatus(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:8081");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");

        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || !pathInfo.equals("/update")) {
                sendError(resp, "无效路径");
                return;
            }

            // 解析前端传递的商品数据
            Goods goods = objectMapper.readValue(req.getInputStream(), Goods.class);

            // 验证核心参数
            if (goods.getId() == null || goods.getId() <= 0) {
                sendError(resp, "商品ID不能为空");
                return;
            }
            if (goods.getName() == null || goods.getName().isEmpty()) {
                sendError(resp, "商品名称不能为空");
                return;
            }
            if (goods.getPrice() == null || goods.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                sendError(resp, "商品价格必须大于0");
                return;
            }

            // 从请求头获取全局会话ID
            String sessionId = req.getHeader("X-Session-Id");
            System.out.println("全局会话ID: " + sessionId); // 调试用

            // 通过全局变量管理器获取用户ID
            Integer userId = util.SessionManager.getInstance().getUserId(sessionId);
            System.out.println("从全局变量获取userId: " + userId); // 调试用

            // 验证会话有效性
            if (sessionId == null || userId == null) {
                sendError(resp, "请先登录");
                return;
            }

            // 调用Service层更新商品
            boolean success = goodsService.updateGoods(goods, userId);
            if (success) {
                sendSuccess(resp, "商品编辑成功");
            } else {
                sendError(resp, "商品编辑失败（权限不足或商品不存在）");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "编辑失败：" + e.getMessage());
        }
    }

    // ------------------- 工具方法：解码 GET 参数 -------------------
    private String decodeGetParam(HttpServletRequest req, String paramName) {
        String paramValue = req.getParameter(paramName);
        if (paramValue == null) {
            return null;
        }
        try {
            // 手动 URL 解码（解决 GET 请求中文乱码）
            return URLDecoder.decode(paramValue, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------- 业务方法 -------------------
    // 1. 获取所有上架商品（商品广场）
    private void getOnSaleGoods(HttpServletResponse resp) throws IOException {
        List<Goods> goodsList = goodsService.getOnSaleGoods();
        sendSuccess(resp, goodsList);
    }

    // 2. 获取商品详情
    private void getGoodsDetail(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int goodsId = Integer.parseInt(req.getParameter("id"));
            // 获取isManage参数（默认false）
            boolean isManage = "true".equals(req.getParameter("isManage"));

            Goods goods = goodsService.getGoodsDetail(goodsId);  // 仍调用原Service方法（含浏览量+1）

            // 普通页面请求：过滤未上架/未审核商品
            if (!isManage) {
                if (goods == null || !"审核通过".equals(goods.getStatus()) || !"已上架".equals(goods.getSaleStatus())) {
                    sendError(resp, "商品不存在或已下架");
                    return;
                }
            }
            // 管理页面请求：不过滤，直接返回所有状态商品（即使未上架）
            else {
                if (goods == null) {
                    sendError(resp, "商品不存在");
                    return;
                }
            }

            // 根据请求类型返回对应商品信息
            sendSuccess(resp, goods);
        } catch (NumberFormatException e) {
            sendError(resp, "无效的商品ID");
        }
    }

    // 3. 按分类查询商品（优化：直接接收分类参数）
    private void getGoodsByCategory(String category, HttpServletResponse resp) throws IOException {
        List<Goods> goodsList = goodsService.getGoodsByCategory(category);
        sendSuccess(resp, goodsList);
    }

    // 4. 搜索商品（优化：直接接收关键词）
    private void searchGoods(String keyword, HttpServletResponse resp) throws IOException {
        List<Goods> goodsList = goodsService.searchGoods(keyword);
        sendSuccess(resp, goodsList);
    }

    // 5. 猜你喜欢
    private void getGuessYouLike(HttpServletResponse resp) throws IOException {
        try {
            List<Goods> goodsList = goodsService.getGuessYouLike();
            if (goodsList.isEmpty()) {
                sendError(resp, "没有找到推荐商品");
                return;
            }
            sendSuccess(resp, goodsList);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "获取推荐商品失败: " + e.getMessage());
        }
    }

    // 6. 二手专区
    private void getSecondHandGoods(HttpServletResponse resp) throws IOException {
        List<Goods> goodsList = goodsService.getGoodsByCategory("二手物品");
        sendSuccess(resp, goodsList);
    }

    // 7. 发布商品
    private void publishGoods(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 从请求头获取全局会话ID
        String sessionId = req.getHeader("X-Session-Id");
        System.out.println("全局会话ID: " + sessionId); // 调试用

        // 通过全局变量管理器获取用户ID
        Integer userId = util.SessionManager.getInstance().getUserId(sessionId);
        System.out.println("从全局变量获取userId: " + userId); // 调试用

        // 验证会话有效性
        if (sessionId == null || userId == null) {
            sendError(resp, "请先登录");
            return;
        }

        try {
            // 解析JSON请求体为Goods对象
            Goods goods = objectMapper.readValue(req.getInputStream(), Goods.class);

            // 1. 校验核心参数
            String name = goods.getName();
            BigDecimal price = goods.getPrice();
            if (name == null || name.isEmpty() || price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                sendError(resp, "商品名称或价格无效");
                return;
            }

            // 2. 强制设置关键字段（从全局变量获取的userId，防止前端伪造）
            goods.setUserId(userId);
            //goods.setCategory("二手物品");

            // 3. 日期参数处理
            if (goods.getDate() != null) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setLenient(false);
                    Date parsedDate = sdf.parse(sdf.format(goods.getDate()));
                    goods.setDate(parsedDate);
                } catch (ParseException e) {
                    sendError(resp, "日期格式错误，应为yyyy-MM-dd");
                    return;
                }
            }

            // 4. 调用Service发布商品
            boolean success = goodsService.publishGoods(goods);
            if (success) {
                sendSuccess(resp, "商品发布成功，等待审核");
            } else {
                sendError(resp, "商品发布失败，请重试");
            }

        } catch (IOException e) {
            sendError(resp, "请求格式错误，请检查参数");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "发布失败：" + e.getMessage());
        }
    }

    // 8. 更新上架状态
    private void updateSaleStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            sendError(resp, "请先登录");
            return;
        }

        try {
            int goodsId = Integer.parseInt(req.getParameter("id"));
            String saleStatus = req.getParameter("saleStatus");
            if (!"已上架".equals(saleStatus) && !"未上架".equals(saleStatus)) {
                sendError(resp, "上架状态无效");
                return;
            }

            boolean success = goodsService.updateSaleStatus(goodsId, saleStatus, userId);
            if (success) {
                sendSuccess(resp, "操作成功");
            } else {
                sendError(resp, "操作失败，权限不足或商品状态异常");
            }
        } catch (NumberFormatException e) {
            sendError(resp, "无效的商品ID");
        }
    }

    // ------------------- 响应工具方法 -------------------
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