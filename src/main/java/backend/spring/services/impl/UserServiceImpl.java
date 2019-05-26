package backend.spring.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import backend.spring.dao.UserDao;
import backend.spring.models.User;
import backend.spring.services.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void save(User user) {
        if (user != null) {
            userDao.save(user);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> userList = userDao.findAll();
        if (userList == null) {
            return new ArrayList<>();
        }
        return userList;
    }

    @Override
    public User findOneDyId(int id) {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email != null) {
            return userDao.findByEmail(email);
        } else return null;

    }
}
