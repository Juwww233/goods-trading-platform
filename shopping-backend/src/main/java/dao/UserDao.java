package dao;

import entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDao{
    int updatePassword(@Param("userId") int userId, @Param("newPassword") String newPassword);
    User findUser(String userName);
    List<User> getAllUsers();
    String getUserRole(String userName);
    int getUserId(String userName);
    void insertUser(User user);
    int updateUser(User user);
    // 新增：分页+搜索查询用户
    List<User> findUsersByPage(
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    // 新增：查询符合条件的用户总数（用于分页）
    int countUsers(@Param("keyword") String keyword);

    // 修改：原deleteUser参数应为id（之前是username，错误）
    int deleteUser(@Param("id") int id);

    User findById(@Param("id") int id);
    User findByUsername(String username);
}