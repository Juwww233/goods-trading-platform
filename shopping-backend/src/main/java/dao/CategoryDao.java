package dao;

import entity.Category;
import java.util.List;

public interface CategoryDao{
    Category selectByName(String name);
    Category selectById(Integer id);
    List<Category> selectAll();
    int insert(Category category);
    int update(Category category);
    int deleteById(Integer id);
}
