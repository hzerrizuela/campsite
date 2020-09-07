package com.campsite.interview.service.integration;

import com.campsite.interview.model.RequestReservation;
import com.campsite.interview.model.ReservationDto;
import com.campsite.interview.model.Status;
import com.campsite.interview.repository.ConfigRepository;
import com.campsite.interview.repository.ReservationRepository;
import com.campsite.interview.service.DateHelper;
import com.campsite.interview.service.DummyObjectsFactory;
import com.campsite.interview.service.PersistenceHelper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.campsite.interview.constant.MessageConstants.UPDATE_MESSAGE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;


/**
 * Test integration between {@link com.campsite.interview.service.ReservationService} and {@link com.campsite.interview.service.DateHelper}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class ReservationServiceIntegrationTest implements DummyObjectsFactory {
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ConfigRepository configRepository;
    @Autowired
    private DateHelper dateHelper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PersistenceHelper persistenceHelper;
    @Mock
    private Clock clock;

    // Some fixed date for the tests
    private final static LocalDate NOW_DATE = LocalDate.of(2020, 9, 5);
    //field that will contain the fixed clock
    private Clock fixedClock;

    private DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private RequestReservation request;

    @Before
    public void setup() {
        // when calling LocalDate.now(clock), return the specified LOCAL_DATE
        fixedClock = Clock.fixed(NOW_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        //delete all reservations before each test
        reservationRepository.deleteAll();
        request = dummyRequestReservation("2020-10-01", "2020-10-02");

    }

    @Test
    public void makeReservationWrongDate() {
        String url = "http://localhost:" + port + "/campsite/reservations";
        String wrongFormatDate = "2020-10-1";
        RequestReservation requestReservation = dummyRequestReservation(wrongFormatDate, "2020-10-02");
        HttpEntity<RequestReservation> request = new HttpEntity<>(requestReservation);
        ResponseEntity<ReservationDto> dto = restTemplate.exchange(url, HttpMethod.POST, request, ReservationDto.class);
        assertThat(dto.getBody().getMessage(), CoreMatchers.not(isEmptyString()));

    }

    @Test
    public void makeTooManyReservations() {
        String url = "http://localhost:" + port + "/campsite/reservations";
        //First request
        HttpEntity<RequestReservation> requestEntity = new HttpEntity<>(request);
        ResponseEntity<ReservationDto> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ReservationDto.class);
        assertFullNameAndHttpStatus(response);
        //Second request - Vary name & lastname
        request.setName("George");
        request.setLastname("Lucas");
        ResponseEntity<ReservationDto> response2 = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ReservationDto.class);
        assertFullNameAndHttpStatus(response2);
        //Third request - Vary name & lastname
        request.setName("John");
        request.setLastname("Lennon");
        ResponseEntity<ReservationDto> response3 = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ReservationDto.class);
        assertFullNameAndHttpStatus(response3);
        //Fourth request - Vary name & lastname
        request.setName("Paul");
        request.setLastname("McCartney");
        ResponseEntity<ReservationDto> response4 = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ReservationDto.class);
        assertFullNameAndHttpStatus(response4);
        //Fifth request - Vary name & lastname
        request.setName("Ringo");
        request.setLastname("Starr");
        ResponseEntity<ReservationDto> response5 = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ReservationDto.class);
        assertFullNameAndHttpStatus(response5);
        assertThat(reservationRepository.countByStatus(Status.IN_USE), equalTo(5L));
        //Sixth attempt - not allowed (limit's 5 per configuration)
        request.setName("George");
        request.setLastname("Harrison");
        ResponseEntity<ReservationDto> response6 = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ReservationDto.class);
        assertThat(response6.getStatusCode(), is(equalTo(HttpStatus.FORBIDDEN)));
    }

    private void assertFullNameAndHttpStatus(ResponseEntity<ReservationDto> response) {
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.CREATED)));
        assertThat(response.getBody().getName(), is(equalTo(request.getName())));
        assertThat(response.getBody().getLastname(), is(equalTo(request.getLastname())));
        assertNull(response.getBody().getMessage());
    }

    @Test
    public void updateReservationWrongYearFormat() {
        String url = "http://localhost:" + port + "/campsite/reservations/" + BOOKING_ID.toString();
        String wrongFormatDate = "20-10-01";
        RequestReservation requestReservation = dummyRequestReservation(wrongFormatDate, "2020-10-02");
        HttpEntity<RequestReservation> requestEntity = new HttpEntity<>(request);
        ResponseEntity<ReservationDto> dto = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ReservationDto.class);
        assertThat(dto.getBody().getMessage(), CoreMatchers.not(isEmptyString()));
    }

    @Test
    public void makeAndupdateReservationOK() {
        String url = "http://localhost:" + port + "/campsite/reservations";
        //Make reservation...
        HttpEntity<RequestReservation> requestEntity = new HttpEntity<>(request);
        ResponseEntity<ReservationDto> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, ReservationDto.class);
        assertThat(response.getStatusCode(), is(equalTo(HttpStatus.CREATED)));
        //..and update it.
        url = "http://localhost:" + port + "/campsite/reservations/" + response.getBody().getBookingID();
        String newArrivalDate = "2020-09-20";
        String newDepartureDate = "2020-09-21";
        RequestReservation requestReservation = dummyRequestReservation(newArrivalDate, newDepartureDate);
        requestReservation.setName("TimUpdated");
        requestReservation.setLastname("LastNameUpdated");
        requestEntity = new HttpEntity<>(requestReservation);
        ResponseEntity<ReservationDto> dto = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ReservationDto.class);
        assertThat(dto.getBody().getMessage(), is(equalTo(UPDATE_MESSAGE)));
        assertThat(dto.getBody().getArrivalDate(), is(equalTo(LocalDate.of(2020, 9, 20))));
        assertThat(dto.getBody().getDepartureDate(), is(equalTo(LocalDate.of(2020, 9, 21))));
        assertThat(dto.getBody().getName(), is(equalTo("TimUpdated")));
        assertThat(dto.getBody().getLastname(), is(equalTo("LastNameUpdated")));
    }
}