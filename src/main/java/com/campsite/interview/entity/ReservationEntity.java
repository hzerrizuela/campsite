package com.campsite.interview.entity;

import com.campsite.interview.model.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "RESERVATIONS")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40)
    private String bookingID;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 200, nullable = false)
    private String lastname;

    @Column(length = 200, nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDate arrivalDate;

    @Column(nullable = false)
    private LocalDate departureDate;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime creationDatetime;

    @Column
    @UpdateTimestamp
    private LocalDateTime modificationDatetime;
    @Column
    @Enumerated(EnumType.STRING)
    private Status status;


    /**
     *
     * @param bookingID
     * @param name
     * @param lastname
     * @param email
     * @param status
     * @param arrivalDate
     * @param departureDate
     */
    public ReservationEntity(String bookingID, String name, String lastname, String email, Status status, LocalDate arrivalDate, LocalDate departureDate) {
        this.bookingID = bookingID;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.status = status;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }
}