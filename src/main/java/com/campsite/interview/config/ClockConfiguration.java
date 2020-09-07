package com.campsite.interview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfiguration {

    @Bean
    public Clock defaultClock() {
        return Clock.systemDefaultZone();
    }

}
