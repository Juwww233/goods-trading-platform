package entity;

public class Admin {
    private int Id;
    private String UserName;
    private String Password;
    private String Name;
    private String Avatar;
    private String Role;
    private String Phone;
    private String Email;

    public Admin(){}

    public int getId() {
        return Id;
    }
    public void setId(int id) {
        this.Id = id;
    }
    public String getUserName() {
        return UserName;
    }
    public void setUserName(String userName) {
        this.UserName = userName;
    }
    public String getPassword() {
        return Password;
    }
    public void setPassword(String password) {
        this.Password = password;
    }
    public String getName() {
        return Name;
    }
    public void setName(String name) {
        this.Name = name;
    }
    public String getAvatar() {
        return Avatar;
    }
    public void setAvatar(String avatar) {
        this.Avatar = avatar;
    }
    public String getRole() {
        return Role;
    }
    public void setRole(String role) {
        this.Role = role;
    }
    public String getPhone() {
        return Phone;
    }
    public void setPhone(String phone) {
        this.Phone = phone;
    }
    public String getEmail() {
        return Email;
    }
    public void setEmail(String email) {
        this.Email = email;
    }
}