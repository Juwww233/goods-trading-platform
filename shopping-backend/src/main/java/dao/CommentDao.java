package dao;

import entity.Comment;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 评论数据访问接口，定义与comment表相关的数据库操作
 */
public interface CommentDao {

    /**
     * 根据商品ID分页查询评论（关联查询用户名）
     * @param goodsId 商品ID
     * @param startIndex 起始索引（分页用）
     * @param pageSize 每页条数（分页用）
     * @return 评论列表（包含用户名、评论内容、时间等）
     */
    List<Map<String, Object>> getCommentsByGoodsId(
            @Param("goodsId") Integer goodsId,
            @Param("startIndex") Integer startIndex,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 查询某商品的评论总数（用于分页计算）
     * @param goodsId 商品ID
     * @return 评论总数
     */
    Integer getCommentCountByGoodsId(@Param("goodsId") Integer goodsId);

    /**
     * 添加新评论
     * @param comment 评论对象（包含content、userId、goodsId、time等）
     * @return 受影响的行数（1表示成功，0表示失败）
     */
    Integer addComment(Comment comment);

    /**
     * 根据用户ID查询该用户的所有评论（关联商品信息）
     * @param userId 用户ID
     * @return 评论列表（包含商品信息、评论内容等）
     */
    List<Map<String, Object>> getCommentsByUserId(@Param("userId") Integer userId);

    /**
     * 根据评论ID删除评论
     * @param commentId 评论ID
     * @return 受影响的行数（1表示成功，0表示失败）
     */
    Integer deleteCommentById(@Param("id") Integer commentId);
}
