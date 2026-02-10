package service;

import dao.AdminDao;
import entity.Admin;
import util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

public class AdminService {

    /**
     * 根据用户名查找管理员
     */
    public Admin findAdmin(String username) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            AdminDao adminDao = session.getMapper(AdminDao.class);
            return adminDao.findAdmin(username);
        }
    }

    /**
     * 根据ID获取管理员信息
     */
    public Admin getAdminById(int adminId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            AdminDao adminDao = session.getMapper(AdminDao.class);
            // 注意：这里需要在AdminDao中添加findById方法（见后续修改）
            return adminDao.findById(adminId);
        }
    }

    /**
     * 更新管理员信息
     */
    public boolean updateAdmin(Admin admin) {
        SqlSession session = null;
        try {
            session = MyBatisUtil.getSession();
            AdminDao adminDao = session.getMapper(AdminDao.class);
            adminDao.updateAdmin(admin);
            session.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (session != null) {
                session.rollback();
            }
            return false;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * 管理员登录验证
     */
    public Admin login(String username, String password) {
        Admin admin = findAdmin(username);
        if (admin != null && admin.getPassword().equals(password)) {
            return admin;
        }
        return null;
    }
}