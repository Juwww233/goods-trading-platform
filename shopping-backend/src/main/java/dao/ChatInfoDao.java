package dao;

import entity.ChatInfo;
import java.util.List;

public interface ChatInfoDao {
    List<ChatInfo> getAllChatInfo();
    ChatInfo getChatInfoById(int id);
    void insertChatInfo(ChatInfo chatInfo);
    void updateChatInfo(ChatInfo chatInfo);
    void deleteChatInfo(int id);
}
