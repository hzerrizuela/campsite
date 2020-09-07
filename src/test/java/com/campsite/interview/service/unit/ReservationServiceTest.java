package com.campsite.interview.service.unit;

import com.campsite.interview.entity.ConfigurationEntity;
import com.campsite.interview.entity.ReservationEntity;
import com.campsite.interview.exception.ReservationException;
import com.campsite.interview.exception.ReservationNonExistingException;
import com.campsite.interview.model.RequestReservation;
import com.campsite.interview.model.ReservationDto;
import com.campsite.interview.model.Status;
import com.campsite.interview.repository.ConfigRepository;
import com.campsite.interview.repository.ReservationRepository;
import com.campsite.interview.service.DateHelper;
import com.campsite.interview.service.DummyObjectsFactory;
import com.campsite.interview.service.PersistenceHelper;
import com.campsite.interview.service.ReservationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.campsite.interview.constant.MessageConstants.CANCEL_MESSAGE;
import static com.campsite.interview.constant.MessageConstants.UPDATE_MESSAGE;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class ReservationServiceTest implements DummyObjectsFactory {
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ConfigRepository configRepository;
    @Mock
    private DateHelper dateHelper;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PersistenceHelper persistenceHelper;
    @Mock
    private Clock clock;

    @InjectMocks
    private ReservationService service = new ReservationService(reservationRepository, dateHelper, configRepository, modelMapper, persistenceHelper, clock);
    private RequestReservation request;
    private DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // Some fixed date for the tests
    private final static LocalDate NOW_DATE = LocalDate.of(2020, 9, 5);
    //field that will contain the fixed clock
    private Clock fixedClock;

    @Before
    public void setup() {
        // when calling LocalDate.now(clock), return the specified LOCAL_DATE
        fixedClock = Clock.fixed(NOW_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();

        request = dummyRequestReservation("2020-10-01", "2020-10-02");
        when(configRepository.getConfiguration()).thenReturn(dummyConfig());
        when(reservationRepository.countByStatus(any(Status.class))).thenReturn(0L).thenReturn(1L);
        ReservationDto reservationDto = dummyReservationDto();
        when(modelMapper.map(any(), any())).thenReturn(reservationDto);
        when(dateHelper.stringToLocalDate(eq(request.getArrivalDate()))).thenReturn(LocalDate.of(2020, 10, 1));
        when(dateHelper.stringToLocalDate(eq(request.getDepartureDate()))).thenReturn(LocalDate.of(2020, 10, 2));

        ReservationEntity differentEntity = dummySecondEntity(Status.IN_USE);
        when(reservationRepository.findByBookingIDAndStatus(anyString(), any(Status.class))).thenReturn(Optional.of(differentEntity));
    }

    @Test
    public void makeReservationOK() {
        ReservationDto reservationDto = service.makeReservation(request);

        assertNotNull(reservationDto);
        verify(persistenceHelper, times(1)).saveConfiguration(any(ConfigurationEntity.class));
        verify(persistenceHelper, times(1)).saveReservation(any(ReservationEntity.class));

        assertThat(reservationDto.getBookingID(), is(not(isEmptyString())));
        LocalDate arrivalDate = LocalDate.parse(request.getArrivalDate(), formatterDate);
        LocalDate departureDate = LocalDate.parse(request.getDepartureDate(), formatterDate);
        assertThat(reservationDto.getArrivalDate(), is(equalTo(arrivalDate)));
        assertThat(reservationDto.getDepartureDate(), is(equalTo(departureDate)));
        assertThat(reservationDto.getEmail(), is(equalTo(request.getEmail())));
        assertThat(reservationDto.getName(), is(equalTo(request.getName())));
        assertThat(reservationDto.getLastname(), is(equalTo(request.getLastname())));
        assertThat(reservationDto.getStatus(), is(equalTo(Status.IN_USE)));
    }

    @Test(expected = ReservationException.class)
    public void attemptReservationThatAlreadyExists() {
        when(reservationRepository.findByNameIgnoreCaseAndLastnameIgnoreCaseAndEmailIgnoreCase(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(dummyEntity()));
        service.makeReservation(request);
    }

    @Test(expected = ReservationException.class)
    public void attemptReservationWithoutEnoughAnticipation() {
        when(dateHelper.stringToLocalDate(eq(request.getArrivalDate()))).thenReturn(NOW_DATE);
        when(dateHelper.stringToLocalDate(eq(request.getDepartureDate()))).thenReturn(NOW_DATE.plusDays(1L));
        service.makeReservation(request);
    }

    @Test(expected = ReservationException.class)
    public void attemptStayMoreThan3Days() {
        when(dateHelper.stringToLocalDate(eq(request.getDepartureDate()))).thenReturn(LocalDate.of(2020, 10, 7));
        service.makeReservation(request);
    }

    @Test(expected = ReservationException.class)
    public void makeReservationNoMoreCapacity() {
        when(reservationRepository.countByStatus(Status.IN_USE)).thenReturn(10L);

        service.makeReservation(request);
    }

    @Test(expected = ReservationNonExistingException.class)
    public void updateReservationNonExisingBookingId() {
        when(reservationRepository.findByBookingIDAndStatus(anyString(), any(Status.class))).thenThrow(ReservationNonExistingException.class);
        service.updateReservation(request, BOOKING_ID);
    }

    @Test
    public void updateReservationOK() {
        ReservationDto dummyDto = dummyReservationWithMessageDto(Status.IN_USE);
        when(modelMapper.map(any(), any())).thenReturn(dummyDto);

        ReservationDto reservationDto = service.updateReservation(request, BOOKING_ID);

        assertThat(reservationDto.getMessage(), is(equalTo(UPDATE_MESSAGE)));
        verify(persistenceHelper, times(1)).saveReservation(any(ReservationEntity.class));
        LocalDate arrivalDate = LocalDate.parse(request.getArrivalDate(), formatterDate);
        LocalDate departureDate = LocalDate.parse(request.getDepartureDate(), formatterDate);
        assertThat(reservationDto.getArrivalDate(), is(equalTo(arrivalDate)));
        assertThat(reservationDto.getDepartureDate(), is(equalTo(departureDate)));
        assertThat(reservationDto.getEmail(), is(equalTo(request.getEmail())));
        assertThat(reservationDto.getName(), is(equalTo(request.getName())));
        assertThat(reservationDto.getLastname(), is(equalTo(request.getLastname())));
        assertThat(reservationDto.getStatus(), is(equalTo(Status.IN_USE)));
        assertThat(reservationDto.getBookingID(), is(equalTo(BOOKING_ID.toString())));
    }

    @Test(expected = ReservationNonExistingException.class)
    public void cancelReservationNonExisting() {
        when(reservationRepository.findByBookingIDAndStatus(anyString(), any(Status.class))).thenThrow(ReservationNonExistingException.class);
        service.cancelReservation(BOOKING_ID);
    }

    @Test
    public void cancelReservationOK() {
        ReservationEntity differentEntity = dummySecondEntity(Status.CANCELLED);
        when(reservationRepository.findByBookingIDAndStatus(anyString(), any(Status.class))).thenReturn(Optional.of(differentEntity));
        ReservationDto dummyDto = dummyReservationWithMessageDto(Status.CANCELLED);
        when(modelMapper.map(any(), any())).thenReturn(dummyDto);

        ReservationDto reservationDto = service.cancelReservation(BOOKING_ID);

        verify(persistenceHelper, times(1)).saveReservation(any(ReservationEntity.class));
        assertThat(reservationDto.getStatus(), is(equalTo(Status.CANCELLED)));
        assertThat(reservationDto.getMessage(), is(equalTo(CANCEL_MESSAGE)));
        assertThat(reservationDto.getBookingID(), is(equalTo(BOOKING_ID.toString())));
    }



}