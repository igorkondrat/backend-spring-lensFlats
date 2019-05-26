package backend.spring.controllers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import backend.spring.dao.FlatDao;
import backend.spring.dao.MessageDao;
import backend.spring.dao.UserDao;
import backend.spring.models.Flat;
import backend.spring.models.Message;
import backend.spring.models.MessageHistory;
import backend.spring.models.User;
import backend.spring.services.MailService;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class CustomRestController {

    private UserDao userDao;
    private PasswordEncoder passwordEncoder;
    private MailService mailService;

    @Autowired
    public CustomRestController(UserDao userDao, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @PostMapping("/setNewPassword")
    public String setNewPassword(@RequestPart("email") String email,
                                 @RequestPart("token") String token,
                                 @RequestPart("password") String password,
                                 @RequestPart("confirmPassword") String confirmPassword) {
        if (password != null && confirmPassword != null && email != null && token != null) {
            if (password.equals(confirmPassword)) {
                User user = userDao.findByEmail(email);
                if (user.getRestorePassword().equals(token)) {
                    user.setPassword(passwordEncoder.encode(password));
                    user.setRestorePassword(null);
                    userDao.save(user);
                    return JSONObject.quote("Password was updated");
                }
            }
            return JSONObject.quote("Password do not match");
        }
        return JSONObject.quote("Something wrong");
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestPart("currentPassword") String s, HttpServletResponse response) {
        String currentRawPassword = s.split("\"")[3];
        String newPassword = s.split("\"")[7];
        String confirmNewPassword = s.split("\"")[11];
        String currentUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userDao.findByEmail(currentUserName);
        if (user != null) {
            if (passwordEncoder.matches(currentRawPassword, user.getPassword())) {
                if (newPassword.equals(confirmNewPassword)) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    String jwtToken = Jwts.builder()
                            .setSubject("!!!" + user.getEmail() + "!!!" + user.getPassword() + "!!!" + user.getRole() + "!!!")
                            .signWith(SignatureAlgorithm.HS512, "q12w".getBytes())
//                            .setExpiration(new Date(System.currentTimeMillis() + 259200000))
                            .compact();
                    response.setHeader("authToken", "Bearer " + jwtToken);
                    userDao.save(user);
                } else return JSONObject.quote("Passwords doesn't matches!");
            } else return JSONObject.quote("Wrong password");
        }
        return JSONObject.quote("Password was updated");
    }

    @PostMapping("/rememberPassword")
    public String rememberPassword(@RequestBody String body) {
        String email = body.substring(body.indexOf(":\"") + 2, body.indexOf("\"}"));
        User user = userDao.findByEmail(email);
        if (user != null) {
            user.setRestorePassword(UUID.randomUUID().toString());
            userDao.save(user);
            mailService.send(user.getEmail(), "Hello," + user.getName() + ". To set new password follow the link:" +
                    "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <title>Title</title>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<a href=\"http://localhost:4200/forgotPassword;" + "email=" + user.getEmail() + ";" + "token=" + user.getRestorePassword() + "\"> Set new password</a>\n"
                    + "</body>\n" +
                    "</html>", "Forgot password?");
        }
        return "";
    }

    public static void setResponseHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE, PATCH");
        response.setHeader("Access-Control-Allow-Headers", "authToken, accountDetails");
        response.setHeader("Access-Control-Expose-Headers", "authToken, accountDetails");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

}
