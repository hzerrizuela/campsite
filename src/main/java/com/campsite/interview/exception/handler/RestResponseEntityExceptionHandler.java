package com.campsite.interview.exception.handler;

import com.campsite.interview.exception.DataLayerException;
import com.campsite.interview.exception.ReservationException;
import com.campsite.interview.exception.ReservationNonExistingException;
import com.campsite.interview.model.ReservationDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.format.DateTimeParseException;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {ReservationException.class})
    protected ResponseEntity<Object> handleExistingReservation(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getLocalizedMessage();
        ReservationDto reservationDto = new ReservationDto(bodyOfResponse);
        return handleExceptionInternal(ex, reservationDto, new HttpHeaders(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(value = {DataLayerException.class})
    protected ResponseEntity<Object> handleDataLayer(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getLocalizedMessage();
        ReservationDto reservationDto = new ReservationDto(bodyOfResponse);
        return handleExceptionInternal(ex, reservationDto, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = {DateTimeParseException.class})
    protected ResponseEntity<Object> handleDateExceptions(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getLocalizedMessage();
        ReservationDto reservationDto = new ReservationDto(bodyOfResponse);
        return handleExceptionInternal(ex, reservationDto, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = {ReservationNonExistingException.class})
    protected ResponseEntity<Object> handleNonExistingException(RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getLocalizedMessage();
        ReservationDto reservationDto = new ReservationDto(bodyOfResponse);
        return handleExceptionInternal(ex, reservationDto, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }


}