package dao;

import entity.Help;
import java.util.List;

public interface HelpDao {
    Help selectById(Integer id);
    List<Help> selectAll();
    int insert(Help help);
    int update(Help help);
    int deleteById(Integer id);
}
