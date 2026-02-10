package entity;

/**
 * 收藏实体类 - 对应数据库collect表
 */
public class Collect {
    private Integer id;        // 收藏记录ID（主键）
    private Integer goodsId;    // 商品ID
    private Integer userId;    // 用户ID

    // 无参构造器
    public Collect() {}

    // 有参构造器
    public Collect(Integer goodsId, Integer userId) {
        this.goodsId = goodsId;
        this.userId = userId;
    }

    // Getter 和 Setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    // 重写toString方法（方便日志和调试）
    @Override
    public String toString() {
        return "Collect{" +
                "id=" + id +
                ", goodsId=" + goodsId +
                ", userId=" + userId +
                '}';
    }
}