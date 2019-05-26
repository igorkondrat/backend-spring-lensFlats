package backend.spring.services;

import backend.spring.models.User;

import java.util.List;

public interface UserService {

    void save(User user);

    List<User> findAll();

    User findOneDyId(int id);

}
