package entity;

import java.math.BigDecimal;
import java.util.Date;

public class Orders {
    private Integer id;
    private String goodsName;       // 商品名称
    private String goodsImg;        // 商品图片
    private BigDecimal goodsPrice;  // 商品价格
    private Integer count;          // 购买数量
    private String orderNo;         // 订单编号
    private BigDecimal total;       // 总价
    private String time;            // 下单时间
    private String payNo;           // 支付单号
    private String payTime;         // 支付时间
    private Integer userId;         // 下单人ID
    private String address;         // 收货地址
    private String phone;           // 联系方式
    private String userName;        // 收货人名称
    private String status;          // 订单状态
    private Integer saleId;         // 卖家ID

    // 状态常量
    public static final String STATUS_PENDING = "待支付";
    public static final String STATUS_PAID = "已支付";
    public static final String STATUS_SHIPPED = "已发货";
    public static final String STATUS_RECEIVED = "已收货";
    public static final String STATUS_CANCELLED = "已取消";

    // 无参构造器
    public Orders() {
    }

    // 带参构造器（根据需要添加）
    public Orders(Integer id, String orderNo, Integer userId, String userName,
                  String address, String phone, BigDecimal total, String status) {
        this.id = id;
        this.orderNo = orderNo;
        this.userId = userId;
        this.userName = userName;
        this.address = address;
        this.phone = phone;
        this.total = total;
        this.status = status;
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsImg() {
        return goodsImg;
    }

    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public BigDecimal getGoodsPrice() {
        return goodsPrice;
    }

    public void setGoodsPrice(BigDecimal goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPayNo() {
        return payNo;
    }

    public void setPayNo(String payNo) {
        this.payNo = payNo;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getSaleId() {
        return saleId;
    }

    public void setSaleId(Integer saleId) {
        this.saleId = saleId;
    }

    // entity.Orders.java
    @Override
    public String toString() {
        return "Orders{" +
                "id=" + id +
                ", goodsName='" + goodsName + '\'' +
                ", goodsImg='" + goodsImg + '\'' +
                ", goodsPrice=" + goodsPrice +
                ", count=" + count +
                ", orderNo='" + orderNo + '\'' +
                ", total=" + total +
                ", time='" + time + '\'' +
                ", payNo='" + payNo + '\'' +
                ", payTime='" + payTime + '\'' +
                ", userId=" + userId +
                ", address='" + address + '\'' +
                ", userName='" + userName + '\'' +
                ", status='" + status + '\'' +
                ", saleId=" + saleId +
                '}';
    }
}