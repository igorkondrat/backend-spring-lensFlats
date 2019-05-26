package backend.spring.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import backend.spring.models.Message;

public interface MessageDao extends JpaRepository<Message, Integer> {
    @Query("select c from Message c where c.id=:id")
    Message getMessageById(@Param("id") Integer id);
}
