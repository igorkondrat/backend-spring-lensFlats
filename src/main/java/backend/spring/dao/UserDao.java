package backend.spring.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import backend.spring.models.User;

public interface UserDao extends JpaRepository<User, Integer> {
    @Query("select c from User c where c.email=:email")
    User findByEmail(@Param("email") String email);

    @Query("select c from User c where c.id=:id")
    User getUserById(@Param("id") Integer id);
}
