package com.campsite.interview.repository;

import com.campsite.interview.entity.ConfigurationEntity;
import com.campsite.interview.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<ConfigurationEntity, Long> {

    @Query(value = "SELECT * FROM CONFIG limit 1", nativeQuery = true)
    ConfigurationEntity getConfiguration();
}
