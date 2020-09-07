package com.campsite.interview.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDto {

    private String bookingID;

    private String name;

    private String lastname;

    private String email;

    private LocalDate arrivalDate;

    private LocalDate departureDate;

    private Status status;

    private String message;

    public ReservationDto(String message) {
        this.message = message;
    }

    /**
     *
     * @param bookingID
     * @param name
     * @param lastname
     * @param email
     * @param arrivalDate
     * @param departureDate
     * @param status
     */
    public ReservationDto(String bookingID, String name, String lastname, String email, LocalDate arrivalDate, LocalDate departureDate, Status status) {
        this.bookingID = bookingID;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.status = status;
    }
}
