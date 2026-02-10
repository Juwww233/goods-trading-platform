package entity;

/**
 * 评论实体类，对应comment数据表
 */
public class Comment {
    private Integer id;         // 反馈id（主键）
    private String content;     // 评论内容
    private Integer userId;     // 评论人ID
    private String time;        // 评论时间
    private Integer goodsId;    // 新增：关联的商品ID（用于查询某商品的评论）

    // 无参构造器
    public Comment() {}

    // 全参构造器
    public Comment(Integer id, String content, Integer userId, String time, Integer goodsId) {
        this.id = id;
        this.content = content;
        this.userId = userId;
        this.time = time;
        this.goodsId = goodsId;
    }

    // getter和setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }
}