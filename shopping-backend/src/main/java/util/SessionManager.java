package util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private static SessionManager instance = new SessionManager();
    private Map<String, Integer> sessionMap = new HashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return instance;
    }

    public String createSession(Integer userId) {
        String sessionId = UUID.randomUUID().toString();
        sessionMap.put(sessionId, userId);
        System.out.println("创建会话: " + sessionId + " → " + userId);
        return sessionId;
    }

    public Integer getUserId(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessionMap.remove(sessionId);
        System.out.println("移除会话: " + sessionId);
    }

    // 修正会话验证逻辑
    public boolean isValidSession(String sessionId, int userId) {
        if (sessionId == null) {
            System.out.println("会话ID为空");
            return false;
        }

        Integer storedUserId = sessionMap.get(sessionId);
        boolean isValid = storedUserId != null && storedUserId == userId;

        System.out.println("验证会话: " + sessionId + " → 用户ID: " + userId + " → 结果: " + isValid);
        return isValid;
    }

    // 添加会话清理方法（可选）
    public void clearExpiredSessions() {
        // 可以实现会话过期逻辑，例如基于时间
        sessionMap.clear(); // 简化示例，实际应根据时间判断
        System.out.println("清理所有会话");
    }
}