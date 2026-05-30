package com.undoschool.booking.controller.parent;

import com.undoschool.booking.dto.BookingResponse;
import com.undoschool.booking.dto.OfferingResponse;
import com.undoschool.booking.security.SecurityUser;
import com.undoschool.booking.security.CurrentUserProvider;
import com.undoschool.booking.service.BookingService;
import com.undoschool.booking.service.OfferingService;
import com.undoschool.booking.service.TimezoneService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ParentController {

    private final OfferingService offeringService;
    private final BookingService bookingService;
    private final CurrentUserProvider currentUserProvider;
    private final TimezoneService timezoneService;

    public ParentController(OfferingService offeringService,
                            BookingService bookingService,
                            CurrentUserProvider currentUserProvider,
                            TimezoneService timezoneService) {
        this.offeringService = offeringService;
        this.bookingService = bookingService;
        this.currentUserProvider = currentUserProvider;
        this.timezoneService = timezoneService;
    }

    @GetMapping("/offerings")
    public List<OfferingResponse> listOfferings(
            @RequestParam(defaultValue = "false") boolean upcoming,
            @RequestHeader(value = "X-Timezone", required = false) String timezoneHeader) {
        SecurityUser user = currentUserProvider.requireAuthenticatedUser();
        ZoneId displayZone = timezoneService.resolveZone(timezoneHeader, user.getTimezone());
        return offeringService.listAvailableOfferings(upcoming, displayZone);
    }

    @PostMapping("/offerings/{offeringId}/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse bookOffering(
            @PathVariable UUID offeringId,
            @RequestHeader(value = "X-Timezone", required = false) String timezoneHeader) {
        SecurityUser user = currentUserProvider.requireAuthenticatedUser();
        ZoneId displayZone = timezoneService.resolveZone(timezoneHeader, user.getTimezone());
        return bookingService.bookOffering(currentUserProvider.requireProfileId(), offeringId, displayZone);
    }

    @GetMapping("/parents/me/bookings")
    public List<BookingResponse> listBookings(
            @RequestParam(defaultValue = "false") boolean upcoming,
            @RequestHeader(value = "X-Timezone", required = false) String timezoneHeader) {
        SecurityUser user = currentUserProvider.requireAuthenticatedUser();
        ZoneId displayZone = timezoneService.resolveZone(timezoneHeader, user.getTimezone());
        return bookingService.getBookingsForParent(currentUserProvider.requireProfileId(), upcoming, displayZone);
    }
}
