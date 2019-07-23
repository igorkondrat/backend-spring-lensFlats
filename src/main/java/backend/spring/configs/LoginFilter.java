package backend.spring.configs;

import backend.spring.controllers.PasswordController;
import backend.spring.dao.UserDao;
import backend.spring.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

public class LoginFilter extends AbstractAuthenticationProcessingFilter {

    private UserDao userDao;
    private UserDetailsService userDetailsService;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthenticationManager authenticationManager;

    LoginFilter(UserDetailsService userDetailsService, AuthenticationManager authenticationManager, UserDao userDao) {
        super(new AntPathRequestMatcher("/login", "POST"));
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userDao = userDao;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AuthenticationException, IOException, ServletException {
        BufferedReader reader = httpServletRequest.getReader();
        String body = "";
        String temp;

        while ((temp = reader.readLine()) != null) {
            body += temp;
        }
        String email = body.substring(body.indexOf(":\"") + 2, body.indexOf("\","));
        String rawPassword = body.substring(body.indexOf("password\":\"") + 11, body.indexOf("\"}"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        User user = userDao.findByEmail(email);

        if (user != null) {
            String username = userDetails.getUsername();
            String encodePassword = userDetails.getPassword();
            if (user.isEnabled()) {
                if (passwordEncoder.matches(rawPassword, encodePassword)) {
                    System.out.println(userDetails.getAuthorities());
                    return new UsernamePasswordAuthenticationToken(username, encodePassword, userDetails.getAuthorities());
                } else {
                    System.out.println("Error incorrect password");
                    return new UsernamePasswordAuthenticationToken(null, "wrongPassword", null);
                }
            } else {
                return new UsernamePasswordAuthenticationToken(null, "nonActive", null);
            }
        } else return new UsernamePasswordAuthenticationToken(null, "userNull", null);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        if (authResult.getCredentials().equals("userNull")) {
            setResponseHeaders(response);
            response.addHeader("accountDetails", "User not exist");
        }
        if (authResult.getCredentials().equals("wrongPassword")) {
            setResponseHeaders(response);
            response.addHeader("accountDetails", "Wrong password or email");
        }
        if (authResult.getCredentials().equals("nonActive")) {
            setResponseHeaders(response);
            response.addHeader("accountDetails", "Account non active. Check your email");
        }
        if (authResult.getCredentials() != null && !authResult.getName().equals("")) {
            String jwtToken = Jwts.builder()
                    .setSubject("!!!" + authResult.getName() + "!!!" + authResult.getCredentials() + "!!!" + authResult.getAuthorities() + "!!!")
                    .signWith(SignatureAlgorithm.HS512, "q12w".getBytes())
//                    .setExpiration(new Date(System.currentTimeMillis() + 259200000))
                    .compact();
            response.setHeader("authToken", "Bearer " + jwtToken);
            setResponseHeaders(response);
        }
    }

    private void setResponseHeaders(HttpServletResponse response) {
        PasswordController.setResponseHeaders(response);
    }

}
