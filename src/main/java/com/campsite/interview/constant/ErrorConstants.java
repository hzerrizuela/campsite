package com.campsite.interview.constant;

public final class ErrorConstants {

    public static final String NO_BOOKINGID_IN_USE = "No reservation available found with that booking ID, or it has already been cancelled.";
    public static final String ALREADY_EXISTS = "Reservation already exists for user. Cancel or modify the existing one. Booking id is: ";
    public static final String DEPARTURE_DATE = "Departure date must be after arrival date.";
    public static final String MAX_DAYS_EXCEEDED = "You cannot stay for more than 3 days.";
    public static final String RESERVATION_ERR_DATE = "Reservation has to be done at least 1 day before to arrival, and up to 1 month before";
    public static final String MAX_REACHED = "Maximum of reservations reached. Try again later.";
    public static final String DATA_LAYER_ERROR = "Error saving data. Please retry later on.";
}
