package backend.spring.configs;

import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.filter.GenericFilterBean;
import backend.spring.dao.UserDao;
import backend.spring.models.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

public class JWTAuthenticationFilter extends GenericFilterBean {

    private UserDao userDao;
    private PasswordEncoder passwordEncoder;

    JWTAuthenticationFilter(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // and check presents of token in header Authorization
        String token = httpServletRequest.getHeader("authToken");
        // if present
        if (token != null) {
            // parse it and retrieve body subject from
            String decoderToken = Jwts.parser()
                    .setSigningKey("q12w".getBytes())
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody()
                    .getSubject();
            //after parse of token we create Authentication object
            String username = decoderToken.split("!!!")[1];
            String password = decoderToken.split("!!!")[2];
            User user = userDao.findByEmail(username);
            if (user != null && password.equals(user.getPassword())) {
                authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "authToken");
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "x-requested-with");
        httpServletResponse.addHeader("Access-Control-Allow-Headers", "content-type");
        httpServletResponse.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        httpServletResponse.addHeader("Content-Type", "undefined");
        httpServletResponse.addHeader("Access-Control-Allow-Headers:", "x-requested-with");
        httpServletResponse.addHeader("Access-Control-Allow-Credentials", "true");
//        httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
        chain.doFilter(request, httpServletResponse);
    }

}
