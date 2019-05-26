package backend.spring.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import backend.spring.models.Flat;

public interface FlatDao extends JpaRepository<Flat, Integer> {
    @Query("select c from Flat c where c.id=:id")
    Flat getFlatById(@Param("id") Integer id);

    @Query("select c from Flat c where c.price=:price")
    Page<Flat> getFlatByPrice(@Param("price") Integer price, Pageable pageable);
}
