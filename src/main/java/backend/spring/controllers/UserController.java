package backend.spring.controllers;

import backend.spring.dao.UserDao;
import backend.spring.models.Flat;
import backend.spring.models.User;
import backend.spring.services.MailService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private UserDao userDao;
    private PasswordEncoder passwordEncoder;
    private MailService mailService;

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public UserController(UserDao userDao, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @GetMapping("/userInfo/{id}")
    public User user(@PathVariable("id") int id) {
        return userDao.getUserById(id);
    }

    @PostMapping("/register")
    public String UserRegister(@RequestBody User user) {
        if (userDao.findByEmail(user.getEmail()) == null) {
            if (!user.getName().isEmpty() &&
                    !user.getPassword().isEmpty() &&
                    !user.getEmail().isEmpty() &&
                    !user.getSurname().isEmpty() &&
                    user.getPassword().length() >= 3) {
                String encode = passwordEncoder.encode(user.getPassword());
                user.setPassword(encode);
                user.setProfilePicture("standartProfilePhoto.png");
                user.setActivateUrl(UUID.randomUUID().toString());
                userDao.save(user);
                mailService.send(user.getEmail(), "Hello," + user.getName() + ". To activate your account follow the link:" +
                        "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>Title</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<a href=\"http://localhost:8080/activate&" + user.getEmail() + "&" + user.getActivateUrl() + "\"> Activate Link</a>\n" +
                        "</body>\n" +
                        "</html>", "Welcome to Lens Flats");
                return JSONObject.quote("Welcome. Check your email to activate account!");
            } else return JSONObject.quote("Something wrong:(");
        } else return JSONObject.quote("This email already taken");
    }

    @GetMapping("/userInfo")
    public User userInfo() {
        return userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @GetMapping("/userFlats")
    public List<Flat> userFlats() {
        return userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getFlatList();
    }

    @PostMapping("/updateUser")
    public String updateUser(@RequestBody User user) {
        User userById = userDao.getUserById(user.getId());
        user.setPassword(userById.getPassword());
        user.setProfilePicture(userById.getProfilePicture());
        user.setActivateUrl(userById.getActivateUrl());
        user.setBirthday(user.getBirthday());
        user.setEnabled(true);
        for (Integer message : userById.getMessages()) {
            user.setMessages(message);
        }
        userDao.save(user);
        return JSONObject.quote("Profile was updated");
    }

    @PostMapping("/changePhoto")
    public String changePhoto(@RequestPart("newPhoto") MultipartFile photo) throws IOException {
        BufferedImage image = ImageIO.read(photo.getInputStream());
        if (image != null) {
            User user = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
            String baseDir = System.getProperty("user.dir");
            String oldPhoto = user.getProfilePicture();
            if (!oldPhoto.equals("standartProfilePhoto.png")) {
                Files.delete(Paths.get(baseDir + uploadPath + File.separator + "usersPicture" + File.separator + oldPhoto));
            }
            File uploadFolder = new File(baseDir + uploadPath + File.separator + "usersPicture");
            if (!uploadFolder.exists()) {
                uploadFolder.mkdir();
            }
            String uuidFile = UUID.randomUUID().toString();
            String resultFileName = uuidFile + photo.getOriginalFilename();
            final File targetFile = new File(baseDir + uploadPath + File.separator + "usersPicture" + File.separator + resultFileName);
            targetFile.createNewFile();
            photo.transferTo(targetFile);
            user.setProfilePicture(resultFileName);
            userDao.save(user);
            return JSONObject.quote("Photo was updated");
        } else return JSONObject.quote("This is not an image file");
    }

}
