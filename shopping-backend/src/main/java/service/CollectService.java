package service;

import dao.CollectDao;
import entity.Collect;
import util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CollectService {

    // 根据用户ID查询收藏列表
    public List<Collect> getByUserId(Integer userId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            CollectDao collectDao = session.getMapper(CollectDao.class);
            return collectDao.selectByUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询收藏列表失败", e);
        }
    }

    // 根据用户ID和商品ID移除收藏（修复参数类型）
    public boolean remove(Integer userId, Integer goodsId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            CollectDao collectDao = session.getMapper(CollectDao.class);

            // 关键点：参数类型改为 Map<String, Object>（与Dao接口匹配）
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);   // Integer 自动向上转型为 Object
            params.put("goodsId", goodsId);   // Integer 自动向上转型为 Object

            // 调用Dao的 deleteByUserAndGood 方法（参数类型匹配）
            int rows = collectDao.deleteByUserAndGood(params);
            session.commit();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("移除收藏失败", e);
        }
    }

    // 添加商品到收藏（修复方法名和参数类型）
    public boolean add(Integer userId, Integer goodsId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            CollectDao collectDao = session.getMapper(CollectDao.class);

            // 关键点1：使用 existsByUserAndGood 方法（与Dao接口一致，而非 countByUserAndGood）
            // 关键点2：参数类型改为 Map<String, Object>
            Map<String, Object> checkParams = new HashMap<>();
            checkParams.put("userId", userId);
            checkParams.put("goodsId", goodsId);

            // 调用Dao的 existsByUserAndGood 方法（方法名与Dao一致）
            int count = collectDao.existsByUserAndGood(checkParams);
            if (count > 0) {
                return false; // 已收藏，返回失败
            }

            // 新增收藏
            Collect collect = new Collect();
            collect.setUserId(userId);
            collect.setGoodsId(goodsId);
            int rows = collectDao.insert(collect);
            session.commit();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("添加收藏失败", e);
        }
    }
}