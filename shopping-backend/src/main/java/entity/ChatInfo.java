package entity;

public class ChatInfo {
    private int id;
    private int chatUserId;
    private int userId;
    private String text;
    private String time;

    public ChatInfo(){}

    public ChatInfo(int id, int chatUserId, int userId, String text, String time) {
        this.id = id;
        this.chatUserId = chatUserId;
        this.userId = userId;
        this.text = text;
        this.time = time;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChatUserId() { return chatUserId; }
    public void setChatUserId(int chatUserId) { this.chatUserId = chatUserId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
