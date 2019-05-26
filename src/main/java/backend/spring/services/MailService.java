package backend.spring.services;

public interface MailService {

    void send(String email, String message, String subject);

}
