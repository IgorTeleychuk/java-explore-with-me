package ru.practicum.main_service.exception;

public class TimeLessTwoHourException extends RuntimeException {
    public TimeLessTwoHourException(String message) {
        super(message);
    }
}
