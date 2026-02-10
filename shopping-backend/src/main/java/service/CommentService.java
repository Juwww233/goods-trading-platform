package service;

import dao.CommentDao;
import entity.Comment;
import org.apache.ibatis.session.SqlSession;
import util.MyBatisUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentService {
    private CommentDao commentDao;
    private SqlSession sqlSession; // 保留SqlSession引用，用于提交事务

    public CommentService() {
        sqlSession = MyBatisUtil.getSession(); // 获取SqlSession
        this.commentDao = sqlSession.getMapper(CommentDao.class);
    }

    public List<Map<String, Object>> getMyComments(Integer userId) {
        try {
            return commentDao.getCommentsByUserId(userId);
        } catch (Exception e) {
            sqlSession.rollback();
            throw new RuntimeException("查询我的评论失败：" + e.getMessage());
        }
    }

    // 新增：删除评论
    public boolean deleteComment(Integer commentId) {
        try {
            int rows = commentDao.deleteCommentById(commentId);
            sqlSession.commit();
            return rows > 0;
        } catch (Exception e) {
            sqlSession.rollback();
            throw new RuntimeException("删除评论失败：" + e.getMessage());
        }
    }
    // 查询商品评论（分页）
    public Map<String, Object> getCommentsByGoodsId(Integer goodsId, Integer page, Integer pageSize) {
        try {
            int startIndex = (page - 1) * pageSize;
            List<Map<String, Object>> comments = commentDao.getCommentsByGoodsId(
                    goodsId,
                    startIndex,
                    pageSize
            );
            int totalCount = commentDao.getCommentCountByGoodsId(goodsId);
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);

            Map<String, Object> result = new HashMap<>();
            result.put("comments", comments);
            result.put("totalCount", totalCount);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            return result;
        } catch (Exception e) {
            sqlSession.rollback(); // 出错时回滚
            throw new RuntimeException("查询评论失败：" + e.getMessage());
        }
    }

    // 添加评论（关键：提交事务）
    public boolean addComment(Comment comment) {
        try {
            int rows = commentDao.addComment(comment);
            sqlSession.commit(); // 显式提交事务（核心！确保数据写入数据库）
            return rows > 0;
        } catch (Exception e) {
            sqlSession.rollback(); // 出错时回滚
            throw new RuntimeException("添加评论失败：" + e.getMessage());
        }
    }
}