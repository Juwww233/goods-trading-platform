package entity;

public class Address {
    private int Id;
    public String Name;
    public String Address;
    public String Phone;
    public String User_Id;

    public Address() {}

    public int getId() {
        return Id;
    }
    public void setId(int id) {
        this.Id = id;
    }
    public String getName() {
        return Name;
    }
    public void setName(String Name) {
        this.Name = Name;
    }
    public String getAddress() {
        return Address;
    }
    public void setAddress(String address) {
        this.Address = address;
    }
    public String getPhone() {
        return Phone;
    }
    public void setPhone(String phone) {
        this.Phone = phone;
    }
    public String getUser_Id() {
        return User_Id;
    }
    public void setUser_Id(String user_Id) {
        this.User_Id = user_Id;
    }
}
