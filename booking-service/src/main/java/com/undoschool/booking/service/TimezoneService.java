package com.undoschool.booking.service;

import com.undoschool.booking.exception.AppException;
import com.undoschool.booking.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class TimezoneService {

    public ZoneId parseZone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Invalid timezone: " + timezone);
        }
    }

    public ZoneId resolveZone(String headerTimezone, String profileTimezone) {
        if (headerTimezone != null && !headerTimezone.isBlank()) {
            return parseZone(headerTimezone.trim());
        }
        if (profileTimezone != null && !profileTimezone.isBlank()) {
            return parseZone(profileTimezone);
        }
        return ZoneOffset.UTC;
    }

    public Instant toInstant(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId).toInstant();
    }

    public String formatInstantUtc(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    public String formatInstantLocal(Instant instant, ZoneId zoneId) {
        return instant.atZone(zoneId).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
