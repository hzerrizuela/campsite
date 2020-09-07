package com.campsite.interview.service;

import com.campsite.interview.entity.ConfigurationEntity;
import com.campsite.interview.entity.ReservationEntity;
import com.campsite.interview.model.RequestReservation;
import com.campsite.interview.model.ReservationDto;
import com.campsite.interview.model.Status;

import java.time.LocalDate;
import java.util.UUID;

import static com.campsite.interview.constant.MessageConstants.UPDATE_MESSAGE;

/**
 * Factory of test double objects
 */
public interface DummyObjectsFactory {

    String EMAIL = "test@hotmail.com";
    LocalDate ARRIVAL_DATE = LocalDate.of(2020, 10, 1);
    LocalDate DEPARTURE_DATE = LocalDate.of(2020, 10, 2);
    String LASTNAME = "Crimson";
    String NAME = "Tim";
    UUID BOOKING_ID = UUID.randomUUID();

    default RequestReservation dummyRequestReservation(String arrivalDate, String departureDate) {
        RequestReservation request = new RequestReservation();
        request.setArrivalDate(arrivalDate);
        request.setDepartureDate(departureDate);
        request.setEmail(EMAIL);
        request.setLastname(LASTNAME);
        request.setName(NAME);
        return request;
    }

    /**
     * Allows max of 10 reservations
     *
     * @return
     */
    default ConfigurationEntity dummyConfig() {
        ConfigurationEntity configurationEntity = new ConfigurationEntity();
        configurationEntity.setId(1L);
        configurationEntity.setMaxReservations(10L);
        return configurationEntity;
    }

    default ReservationEntity dummyEntity() {
        return new ReservationEntity(BOOKING_ID.toString(),
                LASTNAME, NAME, EMAIL, Status.IN_USE, ARRIVAL_DATE,
                DEPARTURE_DATE);
    }

    default ReservationEntity dummySecondEntity(Status status) {
        return new ReservationEntity("44444444-9e93-4b05-84b8-b758a5cf0506",
                "Hernan",
                "Zerrizuela",
                "mymail@gmail.com",
                status,
                LocalDate.of(2020, 2, 15),
                LocalDate.of(2020, 2, 17));
    }

    default ReservationDto dummyReservationDto() {
        return new ReservationDto(BOOKING_ID.toString(), NAME, LASTNAME, EMAIL, ARRIVAL_DATE, DEPARTURE_DATE, Status.IN_USE);
    }

    default ReservationDto dummyReservationWithMessageDto(Status status) {
        return new ReservationDto(BOOKING_ID.toString(), NAME, LASTNAME, EMAIL, ARRIVAL_DATE, DEPARTURE_DATE, status, UPDATE_MESSAGE);
    }

}
