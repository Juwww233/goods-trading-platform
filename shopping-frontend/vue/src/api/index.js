import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    timeout: 5000,
    // 移除withCredentials，无需依赖Cookie
});

// 请求拦截器：添加全局会话ID
api.interceptors.request.use(
    config => {
        // 从localStorage获取会话ID并添加到请求头
        const sessionId = localStorage.getItem('sessionId');
        if (sessionId) {
            config.headers['X-Session-Id'] = sessionId;
        }
        return config;
    },
    error => Promise.reject(error)
);

// 响应拦截器
api.interceptors.response.use(
    response => response.data,
    error => Promise.reject(error)
);

export default api;