package com.undoschool.booking.controller.teacher;

import com.undoschool.booking.domain.Course;
import com.undoschool.booking.dto.AddSessionsRequest;
import com.undoschool.booking.dto.OfferingResponse;
import com.undoschool.booking.dto.CreateCourseRequest;
import com.undoschool.booking.dto.CreateOfferingRequest;
import com.undoschool.booking.security.SecurityUser;
import com.undoschool.booking.security.CurrentUserProvider;
import com.undoschool.booking.service.OfferingService;
import com.undoschool.booking.service.TimezoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teachers")
public class TeacherController {

    private final OfferingService offeringService;
    private final CurrentUserProvider currentUserProvider;
    private final TimezoneService timezoneService;

    public TeacherController(OfferingService offeringService,
                             CurrentUserProvider currentUserProvider,
                             TimezoneService timezoneService) {
        this.offeringService = offeringService;
        this.currentUserProvider = currentUserProvider;
        this.timezoneService = timezoneService;
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, UUID> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        Course course = offeringService.createCourse(currentUserProvider.requireProfileId(), request);
        return Map.of("id", course.getId());
    }

    @PostMapping("/offerings")
    @ResponseStatus(HttpStatus.CREATED)
    public OfferingResponse createOffering(@Valid @RequestBody CreateOfferingRequest request) {
        return offeringService.createOffering(currentUserProvider.requireProfileId(), request);
    }

    @PostMapping("/offerings/{offeringId}/sessions")
    public OfferingResponse addSessions(@PathVariable UUID offeringId,
                                        @Valid @RequestBody AddSessionsRequest request) {
        return offeringService.addSessions(currentUserProvider.requireProfileId(), offeringId, request);
    }

    @GetMapping("/offerings")
    public List<OfferingResponse> listOfferings(
            @RequestParam(defaultValue = "false") boolean upcoming,
            @RequestHeader(value = "X-Timezone", required = false) String timezoneHeader) {
        SecurityUser user = currentUserProvider.requireAuthenticatedUser();
        ZoneId displayZone = timezoneService.resolveZone(timezoneHeader, user.getTimezone());
        return offeringService.listTeacherOfferings(currentUserProvider.requireProfileId(), upcoming, displayZone);
    }
}
