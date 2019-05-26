package backend.spring.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Objects;
import java.util.Properties;

@Configuration
@CrossOrigin(origins = "http://localhost:4200/")
@PropertySource("classpath:application.properties")
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:///" + System.getProperty("user.dir") + uploadPath);
    }

    @Autowired
    Environment env;

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(env.getProperty("email.host"));
        mailSender.setPort(Integer.parseInt(Objects.requireNonNull(env.getProperty("email.port"))));
        mailSender.setUsername(env.getProperty("email.username"));
        mailSender.setPassword(env.getProperty("email.password"));
        Properties properties = mailSender.getJavaMailProperties();
        properties.put(Objects.requireNonNull(env.getProperty("email.protocol")), Objects.requireNonNull(env.getProperty("email.protocol.val")));
        properties.put(Objects.requireNonNull(env.getProperty("email.auth")), Objects.requireNonNull(env.getProperty("email.auth.val")));
        properties.put(Objects.requireNonNull(env.getProperty("email.mail.debug")), Objects.requireNonNull(env.getProperty("email.mail.debug.val")));
        properties.put(Objects.requireNonNull(env.getProperty("email.starttls")), Objects.requireNonNull(env.getProperty("email.starttls.val")));
        return mailSender;
    }

}
