package dao;

import entity.Collect;
import java.util.List;
import java.util.Map;

/**
 * 收藏数据访问接口
 */
public interface CollectDao {
    // 根据用户ID查询收藏列表
    List<Collect> selectByUserId(Integer userId);

    // 根据用户ID和商品ID删除收藏
    int deleteByUserAndGood(Map<String, Object> params);

    // 可选方法：添加收藏
    int insert(Collect collect);

    // 可选方法：查询用户是否已收藏商品
    int existsByUserAndGood(Map<String, Object> params);
}