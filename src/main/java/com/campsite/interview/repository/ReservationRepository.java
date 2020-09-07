package com.campsite.interview.repository;

import com.campsite.interview.entity.ReservationEntity;
import com.campsite.interview.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    Optional<ReservationEntity> findByNameIgnoreCaseAndLastnameIgnoreCaseAndEmailIgnoreCase(String name, String lastName, String email);

    Optional<ReservationEntity> findByBookingIDAndStatus(String bookingID, Status status);

    List<ReservationEntity> findByStatus(Status status);

    Optional<ReservationEntity> findByBookingID(String bookingID);

    long countByStatus(Status status);

}
