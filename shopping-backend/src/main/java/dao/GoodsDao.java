package dao;

import entity.Goods;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GoodsDao {

    // 新增：管理员获取商品列表
    List<Goods> getAdminGoodsList(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    // 新增：计算管理员商品总数
    int countAdminGoods(
            @Param("keyword") String keyword,
            @Param("status") String status
    );

    // 新增：更新商品状态
    int updateGoodsStatus(
            @Param("id") int id,
            @Param("status") String status
    );

    // 新增：更新商品状态和拒绝原因
    int updateGoodsStatusAndReason(
            @Param("id") int id,
            @Param("status") String status,
            @Param("reason") String reason
    );

    Integer getIdByName(@Param("goodsName") String goodsName);

    // 1. 按ID查询商品（商品详情页）
    Goods getGoodsById(int id);

    // 2. 按分类查询商品（类别跳转）
    List<Goods> getGoodsByCategory(String category);

    // 3. 查询所有上架商品（购物广场，需审核通过且已上架）
    List<Goods> getOnSaleGoods();

    // 4. 随机推荐商品（猜你喜欢，限制数量）
    List<Goods> getRandomOnSaleGoods(int limit);

    // 5. 查询二手物品（假设二手是一个分类，或通过额外字段判断）
    List<Goods> getSecondHandGoods();

    // 6. 搜索商品（按名称/详情模糊匹配，仅上架商品）
    List<Goods> searchGoods(String keyword);

    // 7. 新增商品（用户发布商品）
    void addGoods(Goods goods);

    // 8. 更新商品信息（含上架/下架状态）
    void updateGoods(Goods goods);

    // 9. 更新商品浏览量（详情页访问时+1）
    void incrementReadCount(int id);

    // 10. 删除商品（用户/管理员操作）
    void deleteGoods(int id);

    // 11. 查询用户发布的商品（个人中心）
    List<Goods> getGoodsByUserId(int userId);
}