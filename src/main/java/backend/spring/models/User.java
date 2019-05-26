package backend.spring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String surname;
    private String password;
    @Column(unique = true)
    private String email;
    private String profilePicture;
    private LocalDate birthday;
    private String country;
    private String activateUrl;
    private String phone;
    private String restorePassword;
    @Column(columnDefinition = "LONGBLOB")
    private ArrayList<Integer> messages = new ArrayList<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private List<Flat> flatList = new ArrayList<>();

    private boolean enabled = false;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }


    private boolean accountNonExpired = true;

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    @Override
    public boolean isAccountNonExpired() { /*чи час користування моїм акаунтом не вичерпаний*/
        return accountNonExpired;
    }


    private boolean credentialsNonExpired = true;

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }


    private boolean accountNonLocked = true;

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }


    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(role.toString())); /*add roles*/
        return simpleGrantedAuthorities;
    }


    public List<Flat> getFlatList() {
        return flatList;
    }

    public void setFlatList(Flat flat) {
        this.flatList.add(flat);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getActivateUrl() {
        return activateUrl;
    }

    public void setActivateUrl(String activateUrl) {
        this.activateUrl = activateUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Integer> getMessages() {
        return messages;
    }

    public void setMessages(Integer messageId) {
        this.messages.add(messageId);
    }

    public String getRestorePassword() {
        return restorePassword;
    }

    public void setRestorePassword(String restorePassword) {
        this.restorePassword = restorePassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                enabled == user.enabled &&
                accountNonExpired == user.accountNonExpired &&
                credentialsNonExpired == user.credentialsNonExpired &&
                accountNonLocked == user.accountNonLocked &&
                Objects.equals(name, user.name) &&
                Objects.equals(surname, user.surname) &&
                Objects.equals(password, user.password) &&
                Objects.equals(email, user.email) &&
                Objects.equals(profilePicture, user.profilePicture) &&
                Objects.equals(birthday, user.birthday) &&
                Objects.equals(country, user.country) &&
                Objects.equals(activateUrl, user.activateUrl) &&
                Objects.equals(phone, user.phone) &&
                Objects.equals(messages, user.messages) &&
                Objects.equals(flatList, user.flatList) &&
                role == user.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, surname, password, email, profilePicture, birthday, country, activateUrl, phone, messages, flatList, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, role);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", birthday=" + birthday +
                ", country='" + country + '\'' +
                ", activateUrl='" + activateUrl + '\'' +
                ", phone='" + phone + '\'' +
                ", enabled=" + enabled +
                ", accountNonExpired=" + accountNonExpired +
                ", credentialsNonExpired=" + credentialsNonExpired +
                ", accountNonLocked=" + accountNonLocked +
                '}';
    }

}
