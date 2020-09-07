package com.campsite.interview.web;

import com.campsite.interview.model.RequestReservation;
import com.campsite.interview.model.ReservationDto;
import com.campsite.interview.service.ReservationService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/campsite")
public class ReservationController {

    @Autowired
    private ReservationService service;

    @PostMapping("/reservations")
    @ApiOperation(value = "Make a reservation")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Database error"),
            @ApiResponse(code = 201, message = "Reservation created")})
    public ResponseEntity<ReservationDto> makeReservation(@RequestBody @Valid RequestReservation reservation) {
        return new ResponseEntity<>(service.makeReservation(reservation), HttpStatus.CREATED);
    }

    @PatchMapping("/cancellation/{bookingID}")
    @ApiOperation(value = "Cancel a reservation")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Database error"),
            @ApiResponse(code = 404, message = "Reservation not found"),
            @ApiResponse(code = 200, message = "Reservation cancelled")})
    public ResponseEntity<ReservationDto> cancelReservation(@PathVariable UUID bookingID) {
        return new ResponseEntity<>(service.cancelReservation(bookingID), HttpStatus.OK);
    }

    @PutMapping("/reservations/{bookingID}")
    @ApiOperation(value = "Modify a reservation")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Database error"),
            @ApiResponse(code = 200, message = "Reservation updated")})
    public ResponseEntity<ReservationDto> updateReservation(@RequestBody @Valid RequestReservation request, @PathVariable UUID bookingID) {
        return new ResponseEntity<>(service.updateReservation(request, bookingID), HttpStatus.OK);
    }

    @GetMapping("/reservations")
    @ApiOperation(value = "Retrieves all reservations in use")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Database error"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<List<ReservationDto>> getReservations() {
        return new ResponseEntity<>(service.getReservationsInUse(), HttpStatus.OK);
    }

    @GetMapping("/reservations/{bookingID}")
    @ApiOperation(value = "Get reservation by id")
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = "Database error"),
            @ApiResponse(code = 404, message = "Not found"),
            @ApiResponse(code = 200, message = "OK")})
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable UUID bookingID) {
        return new ResponseEntity<>(service.getReservationsById(bookingID), HttpStatus.OK);
    }


}