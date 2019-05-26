package backend.spring.models;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

@Entity
public class Message implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int messageCreator;
    private int messageReceiver;
    private String text;
    private String fromUser;
    private String receiverEmail;
    private String senderEmail;
    @Column(columnDefinition = "LONGBLOB")
    private ArrayList<MessageHistory> messageHistory = new ArrayList<>();

    public Message(int messageCreator, String text, String fromUser, String receiverEmail, String senderEmail, ArrayList<MessageHistory> messageHistory) {
        this.messageCreator = messageCreator;
        this.text = text;
        this.fromUser = fromUser;
        this.receiverEmail = receiverEmail;
        this.senderEmail = senderEmail;
        this.messageHistory = messageHistory;
    }

    public Message() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMessageCreator() {
        return messageCreator;
    }

    public void setMessageCreator(int messageCreator) {
        this.messageCreator = messageCreator;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<MessageHistory> getMessageHistory() {
        return messageHistory;
    }

    public void setMessageHistory(MessageHistory messageHistory) {
        this.messageHistory.add(messageHistory);
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public int getMessageReceiver() {
        return messageReceiver;
    }

    public void setMessageReceiver(int messageReceiver) {
        this.messageReceiver = messageReceiver;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", messageCreator=" + messageCreator +
                ", messageReceiver=" + messageReceiver +
                ", text='" + text + '\'' +
                ", fromUser='" + fromUser + '\'' +
                ", receiverEmail='" + receiverEmail + '\'' +
                ", senderEmail='" + senderEmail + '\'' +
                ", messageHistory=" + messageHistory +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id == message.id &&
                messageCreator == message.messageCreator &&
                messageReceiver == message.messageReceiver &&
                Objects.equals(text, message.text) &&
                Objects.equals(fromUser, message.fromUser) &&
                Objects.equals(receiverEmail, message.receiverEmail) &&
                Objects.equals(senderEmail, message.senderEmail) &&
                Objects.equals(messageHistory, message.messageHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, messageCreator, messageReceiver, text, fromUser, receiverEmail, senderEmail, messageHistory);
    }
}

