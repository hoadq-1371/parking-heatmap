package com.thedunster.jpa.repository;

import com.thedunster.jpa.entities.CitationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface CitationRepository extends CrudRepository<CitationEntity, Long> {

    List<CitationEntity> findById(String id);

    List<CitationEntity> findByCitation(String citation);

    List<CitationEntity> findByDate(Timestamp timeStamp);

    @Query("SELECT c FROM CitationEntity c WHERE c.date BETWEEN :startTime AND :endTime")
    List<CitationEntity> findByRange(@Param("startTime") Timestamp startTime,
                                     @Param("endTime") Timestamp endTime);

}