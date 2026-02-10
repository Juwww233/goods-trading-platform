// vue.config.js
const { defineConfig } = require('@vue/cli-service');

module.exports = defineConfig({
    devServer: {
        port: 8081,
        proxy: {
            '/api': {
                target: 'http://localhost:8080/shopping2',
                changeOrigin: true,
                withCredentials: true,
                pathRewrite: {
                    '^/api': ''
                }
            }
        }
    },
    configureWebpack: {
        resolve: {
            alias: {
                '@': require('path').resolve(__dirname, 'src')
            }
        }
    }
});

