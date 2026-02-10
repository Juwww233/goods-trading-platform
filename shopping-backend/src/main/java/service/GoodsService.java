package service;

import dao.GoodsDao;
import entity.Goods;
import util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoodsService {

    // 新增：获取管理员视角的商品列表（带分页和筛选）
    public Map<String, Object> getAdminGoodsList(String keyword, String status, int page, int pageSize) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);

            // 计算偏移量
            int offset = (page - 1) * pageSize;

            // 查询商品列表
            List<Goods> goodsList = dao.getAdminGoodsList(keyword, status, offset, pageSize);

            // 查询总记录数
            int total = dao.countAdminGoods(keyword, status);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", goodsList);
            result.put("total", total);

            return result;
        }
    }

    // 新增：商品审核通过
    public boolean approveGoods(int goodsId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            Goods goods = dao.getGoodsById(goodsId);
            if (goods == null) return false;

            // 更新为中文状态
            goods.setStatus("审核通过");
            goods.setSaleStatus("已上架");  // 审核通过后自动上架
            dao.updateGoods(goods);
            session.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 新增：商品审核拒绝
    public boolean rejectGoods(int goodsId, String reason) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            Goods goods = dao.getGoodsById(goodsId);
            if (goods == null) return false;

            // 更新为中文状态
            goods.setStatus("审核未通过");
            goods.setSaleStatus("未上架");  // 审核未通过则下架
            goods.setReason(reason);  // 存储拒绝原因
            dao.updateGoods(goods);
            session.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 新增：删除商品（需验证权限）
    public boolean deleteGoods(int goodsId, int operatorUserId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);

            // 1. 查询商品是否存在，验证权限（只能删除自己的商品）
            Goods goods = dao.getGoodsById(goodsId);
            if (goods == null) {
                return false; // 商品不存在
            }
            if (goods.getUserId() != operatorUserId) {
                return false; // 权限不足（不是商品所有者）
            }

            // 2. 执行删除操作
            dao.deleteGoods(goodsId);
            session.commit(); // 提交事务
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除商品失败：" + e.getMessage(), e);
        }
    }

    // 新增：编辑商品信息（名称、价格、content、sale_status）
    public boolean updateGoods(Goods updatedGoods, int operatorUserId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);

            // 1. 查询原商品信息（验证权限）
            Goods originalGoods = dao.getGoodsById(updatedGoods.getId());
            if (originalGoods == null) {
                return false; // 商品不存在
            }
            // 验证：只能编辑自己的商品
            if (originalGoods.getUserId() != operatorUserId) {
                return false; // 权限不足
            }

            // 2. 保留原商品中无需更新的字段（如创建时间、审核状态等）
            updatedGoods.setUserId(originalGoods.getUserId()); // 强制保持创建者ID
            updatedGoods.setStatus(originalGoods.getStatus()); // 审核状态不允许编辑
            updatedGoods.setDate(originalGoods.getDate()); // 创建时间不允许编辑
            updatedGoods.setReadCount(originalGoods.getReadCount()); // 浏览量不允许编辑

            // 3. 更新商品信息
            dao.updateGoods(updatedGoods);
            session.commit();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("编辑商品失败：" + e.getMessage(), e);
        }
    }

    // 新增：根据用户ID查询商品（包含状态）
    public List<Goods> getGoodsByUserId(int userId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            // 直接调用Dao层方法，返回包含状态的商品列表
            return dao.getGoodsByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("根据用户ID查询商品失败：" + e.getMessage(), e);
        }
    }

    public Integer getIdByName(String goodsName) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            return dao.getIdByName(goodsName);
        } catch (Exception e) {
            throw new RuntimeException("查询商品ID失败：" + e.getMessage(), e);
        }
    }

    // 1. 获取商品详情（含浏览量+1）

    public Goods getGoodsById(int goodsId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            return dao.getGoodsById(goodsId); // 直接调用Dao层方法
        }
    }

    public Goods getGoodsDetail(int goodsId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            // 浏览量+1
            dao.incrementReadCount(goodsId);
            // 查询商品详情
            return dao.getGoodsById(goodsId);
        }
    }

    // 2. 按分类查询上架商品
    public List<Goods> getGoodsByCategory(String category) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            return dao.getGoodsByCategory(category);
        }
    }

    // 3. 获取所有上架商品（购物广场）
    public List<Goods> getOnSaleGoods() {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            return dao.getOnSaleGoods();
        }
    }

    // 4. 猜你喜欢（随机推荐5个上架商品）
    public List<Goods> getGuessYouLike() {
        SqlSession session = MyBatisUtil.getSession();
        try {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            return dao.getRandomOnSaleGoods(6);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取猜你喜欢商品失败", e);
        } finally {
            session.close();
        }
    }

    // 5. 搜索商品
    public List<Goods> searchGoods(String keyword) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            return dao.searchGoods(keyword);
        }
    }

    // 6. 发布商品（默认状态：待审核、未上架）
    public boolean publishGoods(Goods goods) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            // 设置默认值（保留其他默认值，移除category的硬编码）
            goods.setStatus("未审核");
            goods.setSaleStatus("未上架");
            goods.setReadCount(0);
            goods.setDate(new Date());
            // 直接使用前端传递的category
            dao.addGoods(goods);
            session.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 7. 上架/下架商品（需审核通过）
    public boolean updateSaleStatus(int goodsId, String saleStatus, int operatorUserId) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            GoodsDao dao = session.getMapper(GoodsDao.class);
            Goods goods = dao.getGoodsById(goodsId);

            // 验证：商品必须属于当前用户，且审核通过
            if (goods.getUserId() != operatorUserId || !"审核通过".equals(goods.getStatus())) {
                return false;
            }

            // 更新上架状态
            goods.setSaleStatus(saleStatus);
            dao.updateGoods(goods);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}