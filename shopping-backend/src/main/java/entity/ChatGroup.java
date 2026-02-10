package entity;

public class ChatGroup {
    private int id;
    private int chatUserId;
    private int userId;

    public ChatGroup(){}

    public ChatGroup(int id, int chatUserId, int userId) {
        this.id = id;
        this.chatUserId = chatUserId;
        this.userId = userId;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getChatUserId() { return chatUserId; }
    public void setChatUserId(int chatUserId) { this.chatUserId = chatUserId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
}
