package com.campsite.interview.service;

import com.campsite.interview.entity.ConfigurationEntity;
import com.campsite.interview.entity.ReservationEntity;
import com.campsite.interview.exception.ReservationException;
import com.campsite.interview.exception.ReservationNonExistingException;
import com.campsite.interview.model.*;
import com.campsite.interview.repository.ConfigRepository;
import com.campsite.interview.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.campsite.interview.constant.ErrorConstants.*;
import static com.campsite.interview.constant.MessageConstants.CANCEL_MESSAGE;
import static com.campsite.interview.constant.MessageConstants.UPDATE_MESSAGE;

/**
 * Service that allows to retrieve/make/modify/cancel reservations
 */
@Service
@Slf4j
public class ReservationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationService.class);
    private ReservationRepository reservationRepository;
    private ConfigRepository configRepository;
    private DateHelper dateHelper;
    private ModelMapper modelMapper;
    private PersistenceHelper persistenceHelper;
    private Clock clock;

    /**
     * @param reservationRepository
     * @param dateHelper
     * @param configRepository
     * @param modelMapper
     * @param persistenceHelper
     */
    public ReservationService(ReservationRepository reservationRepository, DateHelper dateHelper,
                              ConfigRepository configRepository, ModelMapper modelMapper,
                              PersistenceHelper persistenceHelper, Clock defaultClock) {
        this.reservationRepository = reservationRepository;
        this.dateHelper = dateHelper;
        this.configRepository = configRepository;
        this.modelMapper = modelMapper;
        this.persistenceHelper = persistenceHelper;
        this.clock = defaultClock;
    }

    /**
     * Makes a request for the campsite.
     *
     * @param request - request object
     * @return - request information including booking id
     */
    public synchronized ReservationDto makeReservation(RequestReservation request) {
        ConfigurationEntity config = configRepository.getConfiguration();
        long currentReservations = reservationRepository.countByStatus(Status.IN_USE);
        LOGGER.info("Current Reservations = {}", currentReservations);
        if (currentReservations >= config.getMaxReservations()) {
            throw new ReservationException(MAX_REACHED);
        }

        LocalDate arrivalDate = dateHelper.stringToLocalDate(request.getArrivalDate());
        LocalDate departureDate = dateHelper.stringToLocalDate(request.getDepartureDate());

        //check request already exists for that user
        checkReservationExists(request.getName(), request.getLastname(), request.getEmail());
        checkDates(arrivalDate, departureDate);

        ReservationEntity reservation = new ReservationEntity(UUID.randomUUID().toString(), request.getName(),
                request.getLastname(), request.getEmail(), Status.IN_USE, arrivalDate, departureDate);
        ReservationDto reservationDto = modelMapper.map(persistenceHelper.saveReservation(reservation), ReservationDto.class);
        persistenceHelper.saveConfiguration(config);
        LOGGER.info("Reservation with id {} was created successfully.", reservationDto.getBookingID());
        return reservationDto;
    }

    private void checkDates(LocalDate arrivalDate, LocalDate departureDate) {
        checkRequiredAnticipation(arrivalDate);
        checkMaxStayDuration(arrivalDate, departureDate);
    }

    private void checkMaxStayDuration(LocalDate arrivalDate, LocalDate departureDate) {
        if (departureDate.isBefore(arrivalDate)) {
            throw new ReservationException(DEPARTURE_DATE);
        }
        Period period = Period.between(arrivalDate, departureDate);
        if (getTotalDays(period) > 3) {
            throw new ReservationException(MAX_DAYS_EXCEEDED);
        }
    }

    private int getTotalDays(Period period) {
        return period.getDays() + period.getMonths() * 30 + period.getYears() * 365;
    }

    /**
     * Checks if any reservation by the same name + lastname + email exists.
     *
     * @param name     - name
     * @param lastName - last name
     * @param email    -  email
     */
    private void checkReservationExists(String name, String lastName, String email) {
        Optional<ReservationEntity> optReservation = reservationRepository.findByNameIgnoreCaseAndLastnameIgnoreCaseAndEmailIgnoreCase(name, lastName, email);
        if (optReservation.isPresent()) {
            throw new ReservationException(ALREADY_EXISTS + optReservation.get().getBookingID());
        }
    }

    private void checkRequiredAnticipation(LocalDate arrivalDate) {
        LocalDate now = LocalDate.now(clock);
        Period period = Period.between(now, arrivalDate);
        int totalDays = getTotalDays(period);
        if (totalDays < 1 || totalDays > 30) {
            throw new ReservationException(RESERVATION_ERR_DATE);
        }
    }

    /**
     * @param request
     * @param bookingID
     * @return
     */
    public synchronized ReservationDto updateReservation(RequestReservation request, UUID bookingID) {
        ReservationEntity reservation = reservationRepository.findByBookingIDAndStatus(bookingID.toString(), Status.IN_USE)
                .orElseThrow(() -> new ReservationNonExistingException(NO_BOOKINGID_IN_USE));

        Optional.ofNullable(request.getEmail()).ifPresent(reservation::setEmail);
        Optional.ofNullable(request.getName()).ifPresent(reservation::setName);
        Optional.ofNullable(request.getLastname()).ifPresent(reservation::setLastname);
        LocalDate arrivalDate = dateHelper.stringToLocalDate(request.getArrivalDate());
        LocalDate departureDate = dateHelper.stringToLocalDate(request.getDepartureDate());
        checkDates(arrivalDate, departureDate);
        Optional.ofNullable(request.getArrivalDate()).ifPresent(a ->
                reservation.setArrivalDate(dateHelper.stringToLocalDate(request.getArrivalDate())));
        Optional.ofNullable(request.getDepartureDate()).ifPresent(d ->
                reservation.setDepartureDate(dateHelper.stringToLocalDate(request.getDepartureDate())));

        ReservationDto dto = modelMapper.map(persistenceHelper.saveReservation(reservation), ReservationDto.class);
        dto.setMessage(UPDATE_MESSAGE);
        LOGGER.info("Reservation with id {} was updated successfully.", dto.getBookingID());
        return dto;
    }

    /**
     * @param bookingID
     * @return
     */
    public ReservationDto cancelReservation(UUID bookingID) {
        ReservationEntity reservation = reservationRepository.findByBookingIDAndStatus(bookingID.toString(), Status.IN_USE)
                .orElseThrow(() -> new ReservationNonExistingException(NO_BOOKINGID_IN_USE));

        reservation.setStatus(Status.CANCELLED); //cancel makes the space available again
        ReservationDto dto = modelMapper.map(persistenceHelper.saveReservation(reservation), ReservationDto.class);
        dto.setMessage(CANCEL_MESSAGE);
        LOGGER.info("Reservation with id {} was cancelled successfully.", dto.getBookingID());
        return dto;
    }

    /**
     * Retrieves all reservations that are currently in use (not cancelled)
     *
     * @return - the list of reservations
     */
    public List<ReservationDto> getReservationsInUse() {
        return reservationRepository.findByStatus(Status.IN_USE)
                .stream()
                .map(r -> modelMapper.map(r, ReservationDto.class)).collect(Collectors.toList());
    }

    /**
     * Retrieves reservation by id
     *
     * @return - a reservation, if exists
     */
    public ReservationDto getReservationsById(UUID bookingID) {
        ReservationEntity entity = reservationRepository.findByBookingID(bookingID.toString())
                .orElseThrow(() -> new ReservationNonExistingException(NO_BOOKINGID_IN_USE));
        return modelMapper.map(entity, ReservationDto.class);
    }
}


