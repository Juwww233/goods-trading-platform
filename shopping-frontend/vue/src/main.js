import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import axios from "axios";

const app = createApp(App);
app.use(router);

// 确保DOM已加载完成
document.addEventListener('DOMContentLoaded', () => {
    app.mount('#app');
});