package backend.spring.controllers;

import backend.spring.dao.FlatDao;
import backend.spring.dao.MessageDao;
import backend.spring.dao.UserDao;
import backend.spring.models.Flat;
import backend.spring.models.Message;
import backend.spring.models.MessageHistory;
import backend.spring.models.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class MessageController {

    private UserDao userDao;
    private FlatDao flatDao;
    private MessageDao messageDao;

    @Autowired
    public MessageController(UserDao userDao, FlatDao flatDao, MessageDao messageDao) {
        this.userDao = userDao;
        this.flatDao = flatDao;
        this.messageDao = messageDao;
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

}
