package com.thedunster.jpa.repository;

import com.thedunster.jpa.entities.LocationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LocationRepository extends CrudRepository<LocationEntity, Long> {

    List<LocationEntity> findById(String id);
    List<LocationEntity> findByStreetAddress(String streetAddress);

}