package backend.spring.controllers;

import backend.spring.dao.UserDao;
import backend.spring.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LoginController {

    private UserDao userDao;

    @Autowired
    public LoginController(UserDao userDao) {
        this.userDao = userDao;
    }

    @GetMapping("/activate&{email}&{uuid}")
    public String activate(@PathVariable("email") String email,
                           @PathVariable("uuid") String uuid) {
        if (email != null && uuid != null && !email.equals("")) {
            User user = userDao.findByEmail(email);
            if (user != null && !user.isEnabled()) {
                if (user.getActivateUrl().equals(uuid)) {
                    user.setEnabled(true);
                    userDao.save(user);
                    return "redirect:http://localhost:4200/login";
                }
            }
        } else return "error";
        return "error";
    }

}
