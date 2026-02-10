import { createRouter, createWebHashHistory } from 'vue-router';
import MainLayout from '@/layouts/MainLayout.vue';
import MerchantLayout from "@/layouts/MerchantLayout.vue";
import Home from '@/views/Buyer/Home.vue';
import Market from '@/views/Buyer/Market.vue';
import GoodInfo from '@/views/Buyer/GoodInfo.vue';
import Order from '@/views/Buyer/Order.vue';
import OrdersManage from "@/views/Buyer/OrdersManage.vue";
import ShoppingCart from "@/views/Buyer/ShoppingCart.vue";
import OrderInfo from "@/views/Buyer/OrderInfo.vue";
import AuthPage from '@/views/AuthPage.vue';
import CommentsManage from "@/views/Buyer/CommentsManage.vue";
import GoodsManage  from "@/views/Seller/GoodsManage.vue";
import OrdersManageMerchant from "@/views/Seller/OrdersManageMerchant.vue";
import GoodInfoManage from "@/views/Seller/GoodInfoManage.vue";
import MyGoodsManage from "@/views/Buyer/MyGoodsManage.vue";
import SearchResult from "@/views/Buyer/SearchResult.vue";
import OrderInfoMerchant from "@/views/Seller/OrderInfoMerchant.vue";

const routes = [

    {
        path: '/auth',
        name: 'Auth',
        component: AuthPage // 登录/注册页
    },
    {
        path: '/admin',
        name: 'Admin',
        component: () => import('@/views/Admin/Admin.vue'),
        meta: { requiresAuth: true, role: 'admin' }
    },
    {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/Admin/AdminUsers.vue'),
        meta: { requiresAuth: true, role: 'admin' }
    },
    {
        path: '/admin/goods',
        name: 'AdminGoods',
        component: () => import('@/views/Admin/AdminGoods.vue'),
        meta: { requiresAuth: true, role: 'admin' }
    },
    {
        path: '/admin/admingoodsinfo/:id',
        name: 'AdminGoodsInfo',
        component: () => import('@/views/Admin/AdminGoodsInfo.vue'),
        meta: { requiresAuth: true, role: 'admin' }
    },
    {
        path: '/admin/announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/Admin/AdminAnnouncements.vue'),
        meta: { requiresAuth: true, role: 'admin' }
    },
    {
        path: '/publish',
        name: 'Publish',
        component: () => import('@/views/Buyer/PublishGoods.vue')
    },
    {
        path: '/publishmerchant',
        name: 'PublishMerchant',
        component: () => import('@/views/Seller/PublishGoodsMerchant.vue')
    },
    {
        path: '/profile',
        name: 'UserProfile',
        component: () => import('@/views/UserProfile.vue')
    },

    {
        path: '/',
        component: MainLayout, // 父组件（共用布局）
        children: [
            { path: '', name: 'Home', component: Home }, // 首页
            { path: 'category/:type', name: 'Category', component: Market }, // 分类页
            { path: 'order/:id', name: 'Order', component: Order },
            { path: 'orderInfo/:orderId', name: 'OrderInfo', component: OrderInfo },
            { path: 'ordersManage', name: 'OrdersManage', component: OrdersManage },
            { path: 'shoppingCart', name: 'ShoppingCart', component: ShoppingCart },
            { path: 'good/:id', name: 'GoodInfo', component: GoodInfo }, // 商品详情页
            { path: 'commentsManage', name: 'CommentsManage', component: CommentsManage },
            { path: 'mygoodsmanage', name: 'MyGoodsManage',  component: MyGoodsManage },
            { path: '/search', name: 'SearchResult', component: SearchResult},
            { path: 'goodinfomanage/:id', name: 'GoodInfoManage', component: GoodInfoManage, meta: { requiresAuth: true } },
        ]
    },

    {
        path: '/merchant',
        component: MerchantLayout,
        meta: { requiresAuth: true, role: 'seller' }, //  seller 角色才可访问该布局
        children: [
            { path: 'goodsManage', name: 'GoodsManage', component: GoodsManage },
            { path: 'ordersmanagemerchant',name: 'OrdersManageMerchant', component: OrdersManageMerchant },
            { path: 'orderInfo/:orderId', name: 'OrderInfoMerchant', component: OrderInfoMerchant },
        ]
    }
];

const router = createRouter({
    history: createWebHashHistory(),
    routes
});

// 白名单路径：无需登录即可访问的页面
const whiteList = ['/auth'];

router.beforeEach((to, from, next) => {
    console.log('导航守卫触发:', to.path);

    // 检查是否为白名单路径
    if (whiteList.includes(to.path)) {
        console.log('白名单路径，直接放行');
        return next();
    }

    // 检查是否需要登录
    if (to.meta.requiresAuth) {
        const sessionId = localStorage.getItem('sessionId');
        const userId = localStorage.getItem('userId');
        const role = localStorage.getItem('role'); // 获取用户角色

        // 未登录则跳转登录页
        if (!sessionId || !userId) {
            console.log('未登录，跳转至登录页');
            return next({
                name: 'Auth',
                query: { redirect: to.fullPath }
            });
        }

        // 检查角色权限（支持多角色判断）
        if (to.meta.role) {
            // 转为数组支持多角色验证（例如: ['admin', 'seller']）
            const allowedRoles = Array.isArray(to.meta.role)
                ? to.meta.role
                : [to.meta.role];

            // 检查用户角色是否在允许列表中
            if (!allowedRoles.includes(role)) {
                console.log(`权限不足: 需要 ${allowedRoles} 角色，当前 ${role}`);

                // 根据用户角色跳转默认页面
                if (role === 'seller') {
                    return next({ name: 'GoodsManager' });
                } else {
                    return next({ name: 'Home' });
                }
            }
        }
    }

    // 其他情况直接放行
    next();
});

export default router;