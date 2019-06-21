package com.thedunster.jpa.repository;

import com.thedunster.common.CitationCountStat;
import com.thedunster.jpa.entities.CarEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarRepository extends CrudRepository<CarEntity, Long> {

    List<CarEntity> findById(String id);

    @Query("SELECT c FROM CarEntity c WHERE LOWER(c.tag) = LOWER(:tag) AND LOWER(c.state) = Lower(:state) " +
            "AND LOWER(c.make) = Lower(:make)")
    List<CarEntity> findByTagStateAndMake(@Param("tag") String tag,
                                          @Param("state") String state,
                                          @Param("make") String make);

    List<CarEntity> findByMake(String make);

    @Query("SELECT DISTINCT(c.make) FROM CarEntity c")
    List<String> findUniqueMakes();

    @Query("SELECT car.make, COUNT(car.make) AS cnt FROM CitationEntity citation, CarEntity car" +
            " WHERE citation.car=car.id group by car.make order by cnt DESC")
    List<Object[]> getCitationCountByMake();

}