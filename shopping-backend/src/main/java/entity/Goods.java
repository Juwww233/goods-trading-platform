package entity;

import java.math.BigDecimal;
import java.util.Date;

public class Goods {
    private Integer id;
    private String name;
    private BigDecimal price;
    private String content;
    private String address;
    private String img;
    private Date date;
    private String status;
    private String category;
    private Integer userId;
    private String saleStatus;
    private Integer readCount;
    private String sellerName;
    private String reason;
    public Goods() {
    }

    public Goods(Integer id, String name, BigDecimal price, String content, String address,
                 String img, Date date, String status, String category, Integer userId,
                 String saleStatus, Integer readCount) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.content = content;
        this.address = address;
        this.img = img;
        this.date = date;
        this.status = status;
        this.category = category;
        this.userId = userId;
        this.saleStatus = saleStatus;
        this.readCount = readCount;
    }

    // Getter和Setter方法

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(String saleStatus) {
        this.saleStatus = saleStatus;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public void setReadCount(Integer readCount) {
        this.readCount = readCount;
    }
}
