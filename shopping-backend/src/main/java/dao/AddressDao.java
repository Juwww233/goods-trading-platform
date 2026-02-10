package dao;

import entity.Address;
import java.util.List;

public interface AddressDao{
    List<Address> getUserAddresses(int user_id);
    Address getAddressById(int id);
    void insertAddress(Address address);
    void updateAddress(Address address);
    void deleteAddress(int id);
}