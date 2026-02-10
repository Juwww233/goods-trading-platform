package dao;

import entity.ChatGroup;
import java.util.List;

public interface ChatGroupDao {
    List<ChatGroup> getAllChatGroups();
    ChatGroup getChatGroupById(int id);
    void insertChatGroup(ChatGroup chatGroup);
    void updateChatGroup(ChatGroup chatGroup);
    void deleteChatGroup(int id);
}
