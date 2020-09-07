package com.campsite.interview.service.unit;

import com.campsite.interview.model.RequestReservation;
import com.campsite.interview.model.ReservationDto;
import com.campsite.interview.repository.ConfigRepository;
import com.campsite.interview.repository.ReservationRepository;
import com.campsite.interview.service.DateHelper;
import com.campsite.interview.service.DummyObjectsFactory;
import com.campsite.interview.service.PersistenceHelper;
import com.campsite.interview.service.ReservationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(locations = {"classpath:spring-test-config.xml"})
public class ReservationServiceConcurrentTest extends AbstractTestNGSpringContextTests implements DummyObjectsFactory {
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
    @Autowired
    private ReservationService service;

    private RequestReservation request;
    private DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // Some fixed date for the tests
    private final static LocalDate NOW_DATE = LocalDate.of(2020, 9, 5);
    //field that will contain the fixed clock
    private Clock fixedClock;

    @BeforeTest
    public void setup() {
        // when calling LocalDate.now(clock), return the specified LOCAL_DATE
        fixedClock = Clock.fixed(NOW_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        request = dummyRequestReservation("2020-10-01", "2020-10-02");
    }

    @Test(threadPoolSize = 3, invocationCount = 5, timeOut = 10000, successPercentage = 20)
    public void make5ConcurrentReservationOK() {
        ReservationDto reservationDto = service.makeReservation(request);
        List<ReservationDto> reservationsInUse = service.getReservationsInUse();
        assertThat(reservationsInUse, hasSize(1));
        System.out.println("reservationDto: " + reservationDto);
    }


}