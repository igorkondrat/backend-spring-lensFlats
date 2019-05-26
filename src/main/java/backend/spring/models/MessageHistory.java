package backend.spring.models;

import java.io.Serializable;
import java.util.Objects;

public class MessageHistory implements Serializable {

    private String messageSender;
    private String message;

    public MessageHistory(String messageSender, String message) {
        this.messageSender = messageSender;
        this.message = message;
    }

    public String getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(String messageSender) {
        this.messageSender = messageSender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageHistory{" +
                "messageSender='" + messageSender + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageHistory that = (MessageHistory) o;
        return Objects.equals(messageSender, that.messageSender) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageSender, message);
    }
}
