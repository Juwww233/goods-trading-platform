package dao;

import entity.Notice;
import java.util.List;
import java.util.Map;

public interface NoticeDao{
    List<Notice> getAllNotice();
    List<Notice> getNoticeByUser(String userName);
    void insertNotice(Notice notice);
    void deleteNotice(int id);
    // 编辑公告（新增）
    void updateNotice(Notice notice);

    // 分页查询（新增，支持条件）
    List<Notice> getNoticeByPage(Map<String, Object> params);

    // 查询总数（配合分页，新增）
    int countNotice(Map<String, Object> params);
}