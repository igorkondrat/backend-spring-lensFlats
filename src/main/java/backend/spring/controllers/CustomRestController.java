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
    private FlatDao flatDao;
    private MessageDao messageDao;
    private MailService mailService;

    @Autowired
    public CustomRestController(UserDao userDao, PasswordEncoder passwordEncoder, FlatDao flatDao, MessageDao messageDao, MailService mailService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.flatDao = flatDao;
        this.messageDao = messageDao;
        this.mailService = mailService;
    }

    @GetMapping("/getAllFlats")
    public List<Flat> flats() {
        return flatDao.findAll();
    }

    @GetMapping("/getFilter/{rating}&{minPrice}&{maxPrice}&{rooms}")
    public List<Flat> starFilter(@PathVariable("rating") int rating,
                                 @PathVariable("minPrice") int minPrice,
                                 @PathVariable("maxPrice") int maxPrice,
                                 @PathVariable("rooms") int rooms) {
        List<Flat> filteredFlatList = flatDao.findAll()
                .stream()
                .filter(flat -> flat.getAverageRating() != null && flat.getAverageRating() >= rating)
                .filter(flat -> flat.getPrice() >= minPrice)
                .filter(flat -> flat.getPrice() <= maxPrice)
                .collect(Collectors.toList());
        if (rooms != 0) {
            if (rooms == 5) {
                filteredFlatList.removeIf(next -> next.getRooms() < rooms);
            } else filteredFlatList.removeIf(next -> next.getRooms() != rooms);
        }
        return filteredFlatList;
    }

    @GetMapping("/getSingleFlatAnonymous/{flatId}")
    public Flat singleFlatAnonymous(@PathVariable("flatId") Integer flatId) {
        return flatDao.getFlatById(flatId);
    }

    @Value("${upload.path}")
    private String uploadPath;

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

    @PostMapping("/flatRegister")
    public String FlatRegister(@RequestBody Flat flat) {
        User user = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null && flat.getRooms() > 0 && flat.getSquare() > 0) {
            flat.setUser(user);
            flat.setPhotoFlats("standartFlatPicture.png");
            flat.setAverageRating(0.0);
            flatDao.save(flat);
            return JSONObject.quote("You created flat");
        } else return JSONObject.quote("Something wrong :(");
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

    @GetMapping("/userInfo")
    public User userInfo() {
        return userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @GetMapping("/userFlats")
    public List<Flat> userFlats() {
        return userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getFlatList();
    }

    @PostMapping("/changePhotoFlat{id}")
    public String changePhotoFlat(@RequestPart("newPhotoFlat") MultipartFile[] photo,
                                  @PathVariable Integer id) throws IOException {
        User user = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        String baseDir = System.getProperty("user.dir");
        File uploadFolder = new File(baseDir + uploadPath + "flatsPicture");

        if (!uploadFolder.exists()) {
            uploadFolder.mkdir();
        }
        for (MultipartFile multipartFile : photo) {
            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
            if (image != null) {
                for (int j = 0; j < user.getFlatList().size(); j++) {
                    if (user.getFlatList().get(j).getId() == id) {
                        Flat flat = user.getFlatList().get(j);
                        if (flat.getListPhotoFlats().size() < 10) {
                            if (flat.getListPhotoFlats().contains("standartFlatPicture.png")) {
                                System.out.println("CustomRest/changePhotoFlat | standart flat photo remove");
                                flat.getListPhotoFlats().removeIf(next -> next.equals("standartFlatPicture.png"));
                            }
                            String uuidFile = UUID.randomUUID().toString();
                            String resultFileName = uuidFile + multipartFile.getOriginalFilename();
                            final File targetFile = new File(baseDir + uploadPath + File.separator + "flatsPicture" + File.separator + resultFileName);
                            targetFile.createNewFile();
                            multipartFile.transferTo(targetFile);
                            flat.setPhotoFlats(resultFileName);
                            flatDao.save(flat);
                            System.out.println("CustomRest/changePhotoFlat | flat photo update");
                        } else return JSONObject.quote("Maximum 10 photo");
                    }
                }
            } else return JSONObject.quote("This is not an image file");
        }
        return JSONObject.quote("Photo was updated");
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

    @PostMapping("/updateFlat")
    public String updateUser(@RequestBody Flat flat) {
        flat.setUser(userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()));
        for (String flatPhoto : flatDao.getFlatById(flat.getId()).getListPhotoFlats()) {
            flat.setPhotoFlats(flatPhoto);
        }
        Flat flatById = flatDao.getFlatById(flat.getId());
        if (flatById != null && flat.getSquare() > 0 && flat.getRooms() > 0 && flat.getPrice() > 0 && flat.getFloor() > 0
                && flat.getGuests() > 0 && flat.getStoreys() > 0) {
            flat.setAverageRating(flatById.getAverageRating());
            for (Double aDouble : flatById.getRating()) {
                flat.setRating(aDouble);
            }
            flatDao.save(flat);
            return JSONObject.quote("Flat was updated");
        }
        return JSONObject.quote("Something wrong");
    }

    @GetMapping("/rateFlat/{flatId}/{rating}")
    public String rate(@PathVariable("flatId") Integer flatId,
                       @PathVariable("rating") double newRating) {
        Flat flat = flatDao.getFlatById(flatId);
        if (flat != null) {
            flat.setRating(newRating);
            ArrayList<Double> rating = flat.getRating();
            double sum = 0;
            for (Double rate : rating) {
                sum += rate;
            }
            flat.setAverageRating(sum / rating.size());
            flatDao.save(flat);
        }
        return "";
    }

    @GetMapping("/getAllMessages")
    public List<Message> getAllMessages() {
        User user = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Message> messageList = new ArrayList<>();
        if (user != null) {
            for (Integer messagesId : user.getMessages()) {
                messageList.add(messageDao.getMessageById(messagesId));
            }
            return messageList;
        }
        return null;
    }

    @GetMapping("/getSingleMessages/{messageId}")
    public Message getSingleMessages(@PathVariable("messageId") Integer messageId) {
        User user = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user != null) {
            for (Integer message : user.getMessages()) {
                if (messageId.equals(message)) {
                    return messageDao.getMessageById(message);
                }
            }
        }
        return null;
    }

    @PostMapping("/sendMessage")
    public void sendMessage(@RequestBody Message message) {
        Message messageById = messageDao.getMessageById(message.getId());
        if (messageById != null) {
            User userFrom = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
            if (userFrom != null) {
                MessageHistory messageHistory = new MessageHistory(userFrom.getSurname() + " "
                        + userFrom.getName(), message.getText());
                messageById.setMessageHistory(messageHistory);
                messageDao.save(messageById);
            }
        }
    }

    @PostMapping("/createMessage")
    public String createMessage(@RequestBody Message message) {
        User userFrom = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        User userFor = userDao.findByEmail(message.getReceiverEmail());
        if (userFrom != null && userFor != null) {
            MessageHistory messageHistory = new MessageHistory(userFrom.getSurname() + " "
                    + userFrom.getName(), message.getText());
            message.setMessageCreator(userFrom.getId());
            message.setMessageReceiver(userFor.getId());
            message.setFromUser(userFrom.getSurname() + " "
                    + userFrom.getName());
            message.setMessageHistory(messageHistory);
            message.setSenderEmail(userFrom.getEmail());
            messageDao.save(message);
            userFrom.setMessages(message.getId());
            userFor.setMessages(message.getId());
            userDao.save(userFrom);
            userDao.save(userFor);
            return JSONObject.quote("Message send");
        } else return JSONObject.quote("User not found");
    }

    @GetMapping("/getOwnerEmail/{flatId}")
    public ArrayList<String> getOwnerEmail(@PathVariable("flatId") int flatId) {
        ArrayList<String> list = new ArrayList<>();
        Flat flat = flatDao.getFlatById(flatId);
        if (flat != null) {
            list.add(flat.getUser().getEmail());
            list.add(flat.getUser().getPhone());
            return list;
        }
        return null;
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

    @PostMapping("/deleteFlatPhoto/{flatPhoto}&{flatId}")
    public String deleteFlatPhoto(@PathVariable("flatPhoto") String flatPhoto,
                                  @PathVariable("flatId") Integer flatId) throws IOException {
        User user = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        String baseDir = System.getProperty("user.dir");
        if (user != null) {
            List<Flat> flatList = user.getFlatList();
            for (Flat flat : flatList) {
                if (flat.getId() == flatId) {
                    flat.getListPhotoFlats().removeIf(photo -> photo.equals(flatPhoto));
                    if (flat.getListPhotoFlats().size() < 1) {
                        flat.setPhotoFlats("standartFlatPicture.png");
                    }
                }
            }
            deleteAllFlatPhoto(baseDir, flatPhoto);
            userDao.save(user);
        }
        return JSONObject.quote("Photo was removed");
    }

    @PostMapping("/deleteFlat{flatId}")
    public void deleteFlat(@PathVariable Integer flatId) throws IOException {
        User user = userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        List<Flat> flatList = user.getFlatList();
        String baseDir = System.getProperty("user.dir");
        for (Flat flat1 : flatList) {
            if (flat1.getId() == flatId) {
                ArrayList<String> listPhotoFlats = flat1.getListPhotoFlats();
                for (String flatPhoto : listPhotoFlats) {
                    deleteAllFlatPhoto(baseDir, flatPhoto);
                }
            }
        }
        user.getFlatList().removeIf(flat -> flat.getId() == flatId);
        flatDao.delete(flatDao.getFlatById(flatId));
        userDao.save(user);
    }

    @GetMapping("/getSingleFlat/{flatId}")
    public Flat singleFlat(@PathVariable("flatId") Integer flatId) {
        for (Flat flat : userDao.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getFlatList()) {
            if (flat.getId() == flatId) {
                return flat;
            }
        }
        return null;
    }

    @GetMapping("/userInfo/{id}")
    public User user(@PathVariable("id") int id) {
        return userDao.getUserById(id);
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

    private void deleteAllFlatPhoto(String baseDir, String listPhotoFlat) throws IOException {
        if (Files.exists(Paths.get(baseDir + uploadPath + File.separator + "flatsPicture" + File.separator + listPhotoFlat)) && !listPhotoFlat.equals("standartFlatPicture.png")) {
            Files.delete(Paths.get(baseDir + uploadPath + File.separator + "flatsPicture" + File.separator + listPhotoFlat));
        }
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
