package com.thedunster.jpa.repository;

import com.thedunster.jpa.entities.ViolationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ViolationRepository extends CrudRepository<ViolationEntity, Long> {

    List<ViolationEntity> findById(String id);
    List<ViolationEntity> findByDescription(String description);


}