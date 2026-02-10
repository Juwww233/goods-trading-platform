package service;

import dao.NoticeDao;
import entity.Notice;
import util.MyBatisUtil;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeService {
    // 分页查询（返回包含list和total的Map）
    public Map<String, Object> getNoticeList(int page, int pageSize, String keyword) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            NoticeDao dao = session.getMapper(NoticeDao.class);
            Map<String, Object> params = new HashMap<>();
            params.put("title", keyword);
            params.put("offset", (page - 1) * pageSize);
            params.put("pageSize", pageSize);
            // 查询列表和总数
            List<Notice> list = dao.getNoticeByPage(params);
            int total = dao.countNotice(params);
            // 封装结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);
            return result;
        }
    }

    // 创建公告
    public void createNotice(Notice notice) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            NoticeDao dao = session.getMapper(NoticeDao.class);
            dao.insertNotice(notice);
            session.commit();
        }
    }

    // 更新公告
    public void updateNotice(Notice notice) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            NoticeDao dao = session.getMapper(NoticeDao.class);
            dao.updateNotice(notice);
            session.commit();
        }
    }

    // 删除公告
    public void deleteNotice(int id) {
        try (SqlSession session = MyBatisUtil.getSession()) {
            NoticeDao dao = session.getMapper(NoticeDao.class);
            dao.deleteNotice(id);
            session.commit();
        }
    }

}