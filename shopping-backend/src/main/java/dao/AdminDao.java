package dao;

import entity.Admin;

public interface AdminDao{
    Admin findAdmin(String userName);
    void updateAdmin(Admin admin);
    Admin findById(int id);
}