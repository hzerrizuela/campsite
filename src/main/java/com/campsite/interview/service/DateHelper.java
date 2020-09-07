package com.campsite.interview.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class DateHelper {

    private DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * @param localDate
     * @return - Date in string format with pattern yyyy-MM-dd
     */
    public String localDateToString(LocalDate localDate) {
        return localDate.format(formatterDate);
    }

    /**
     *
     * @param str - String in format "yyyy-MM-dd"
     * @return str date converted to localDate
     */
    public LocalDate stringToLocalDate(String str) {
        return LocalDate.parse(str, formatterDate);
    }

}
