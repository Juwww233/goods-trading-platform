package service;

import dao.AdminDao;
import dao.UserDao;
import entity.Admin;
import entity.User;
import util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            UserDao userDao = session.getMapper(UserDao.class);

            // 1. 查询用户当前密码
            User user = userDao.findById(userId);
            if (user == null) {
                return false; // 用户不存在
            }

            // 2. 验证原密码
            if (!user.getPassword().equals(oldPassword)) {
                return false; // 原密码错误
            }

            // 3. 更新为新密码
            int rows = userDao.updatePassword(userId, newPassword);
            session.commit(); // 提交事务
            return rows > 0; // 返回是否更新成功
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // UserService.java 中 login 方法修正
    public User login(String username, String password) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            UserDao userDao = session.getMapper(UserDao.class);
            AdminDao adminDao = session.getMapper(AdminDao.class);

            // 1. 先查user表（包含普通用户和商家seller）
            User user = userDao.findUser(username);
            if (user != null && user.getPassword().equals(password)) {
                // 关键修改：不强制设置角色，使用数据库中存储的实际角色（如seller）
                // 移除 user.setRole("user"); 这行代码
                return user; // 直接返回数据库中的用户信息（包含正确角色）
            }

            // 2. 再查Admin表（管理员）
            Admin admin = adminDao.findAdmin(username);
            if (admin != null && admin.getPassword().equals(password)) {
                User adminUser = new User();
                adminUser.setUserName(admin.getUserName());
                adminUser.setPassword(admin.getPassword());
                adminUser.setId(admin.getId());
                adminUser.setRole("admin"); // 管理员角色仍需手动设置（如果admin表无role字段）
                return adminUser;
            }

            // 3. 都没找到
            return null;
        }
    }

    // 注册用户（明文存储密码）
    public boolean register(User user) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            UserDao userDao = session.getMapper(UserDao.class);
            // 检查用户名是否已存在
            session.clearCache();

            if (userDao.findUser(user.getUserName()) != null) {
                return false;
            }

            // 设置默认头像（关键修改）
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                user.setAvatar("default_avatar.jpg"); // 默认头像路径
            }

            // 根据前端传的角色值转换为对应的 seller 或 buyer
            if ("merchant".equals(user.getRole())) {
                user.setRole("seller");
            } else if ("user".equals(user.getRole())) {
                user.setRole("buyer");
            }

            userDao.insertUser(user);
            session.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserById(int userId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            UserDao userDao = session.getMapper(UserDao.class);
            User user = userDao.findById(userId);
            System.out.println("查询到的用户信息: " + user); // 打印用户对象
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateUser(User user) {
        SqlSession session = null;
        try {
            // 获取SqlSession
            session = MyBatisUtil.getSession();
            // 获取DAO接口
            UserDao userDao = session.getMapper(UserDao.class);
            // 执行更新（返回受影响的行数）
            int rows = userDao.updateUser(user);
            // 提交事务（关键：MyBatis默认不自动提交）
            session.commit();
            // 受影响行数>0表示更新成功
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // 异常时回滚事务
            if (session != null) {
                session.rollback();
            }
            return false;
        } finally {
            // 关闭会话（释放资源）
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean updateAvatar(int userId, String avatar) {
        // 实现更新用户头像的业务逻辑
        return false;
    }

    public java.util.Map<String, Object> getUsersByPage(String keyword, int page, int pageSize) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            UserDao userDao = session.getMapper(UserDao.class);

            // 计算分页偏移量
            int offset = (page - 1) * pageSize;

            // 查询当前页数据和总条数
            List<User> list = userDao.findUsersByPage(keyword, offset, pageSize);
            int total = userDao.countUsers(keyword);

            // 封装结果（前端需要list和total）
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * 新增：通过ID删除用户
     */
    public boolean deleteUserById(int userId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            UserDao userDao = session.getMapper(UserDao.class);
            int rows = userDao.deleteUser(userId); // 注意：原UserDao的deleteUser参数是id
            session.commit();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}