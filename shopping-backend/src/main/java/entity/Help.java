package entity;

public class Help {
    private Integer id;
    private String title;
    private String content;
    private String img;
    private String status;
    private Integer userId;
    private String time;
    private String solved;

    public Help() {
    }

    public Help(Integer id, String title, String content, String img, String status,
                Integer userId, String time, String solved) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.img = img;
        this.status = status;
        this.userId = userId;
        this.time = time;
        this.solved = solved;
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getSolved() {
        return solved;
    }

    public void setSolved(String solved) {
        this.solved = solved;
    }
}
