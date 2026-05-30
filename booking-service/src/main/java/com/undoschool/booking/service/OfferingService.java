package com.undoschool.booking.service;

import com.undoschool.booking.domain.ClassSession;
import com.undoschool.booking.domain.Course;
import com.undoschool.booking.domain.Offering;
import com.undoschool.booking.domain.OfferingStatus;
import com.undoschool.booking.domain.Role;
import com.undoschool.booking.domain.UserProfile;
import com.undoschool.booking.dto.AddSessionsRequest;
import com.undoschool.booking.dto.CreateCourseRequest;
import com.undoschool.booking.dto.CreateOfferingRequest;
import com.undoschool.booking.dto.OfferingResponse;
import com.undoschool.booking.exception.AppException;
import com.undoschool.booking.exception.ErrorCode;
import com.undoschool.booking.mapper.OfferingResponseMapper;
import com.undoschool.booking.repository.ClassSessionRepository;
import com.undoschool.booking.repository.CourseRepository;
import com.undoschool.booking.repository.OfferingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OfferingService {

    private final CourseRepository courseRepository;
    private final OfferingRepository offeringRepository;
    private final ClassSessionRepository classSessionRepository;
    private final UserProfileService userProfileService;
    private final TimezoneService timezoneService;
    private final OfferingResponseMapper offeringResponseMapper;

    public OfferingService(CourseRepository courseRepository,
                           OfferingRepository offeringRepository,
                           ClassSessionRepository classSessionRepository,
                           UserProfileService userProfileService,
                           TimezoneService timezoneService,
                           OfferingResponseMapper offeringResponseMapper) {
        this.courseRepository = courseRepository;
        this.offeringRepository = offeringRepository;
        this.classSessionRepository = classSessionRepository;
        this.userProfileService = userProfileService;
        this.timezoneService = timezoneService;
        this.offeringResponseMapper = offeringResponseMapper;
    }

    @Transactional
    public Course createCourse(UUID teacherId, CreateCourseRequest request) {
        UserProfile teacher = userProfileService.requireProfile(teacherId, Role.TEACHER);
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .teacher(teacher)
                .build();
        return courseRepository.save(course);
    }

    @Transactional
    public OfferingResponse createOffering(UUID teacherId, CreateOfferingRequest request) {
        UserProfile teacher = userProfileService.requireProfile(teacherId, Role.TEACHER);
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Course not found"));

        if (!course.getTeacher().getId().equals(teacherId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Course does not belong to teacher");
        }

        ZoneId teacherZone = timezoneService.parseZone(request.getTeacherTimezone());
        OfferingStatus status = request.getStatus() != null ? request.getStatus() : OfferingStatus.DRAFT;

        Offering offering = Offering.builder()
                .course(course)
                .teacher(teacher)
                .name(request.getName())
                .teacherTimezone(teacherZone.getId())
                .status(status)
                .build();

        offering = offeringRepository.save(offering);
        return offeringResponseMapper.toResponse(offering, teacherZone);
    }

    @Transactional
    public OfferingResponse addSessions(UUID teacherId, UUID offeringId, AddSessionsRequest request) {
        Offering offering = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Offering not found"));

        if (!offering.getTeacher().getId().equals(teacherId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Offering does not belong to teacher");
        }

        if (offering.getStatus() == OfferingStatus.PUBLISHED && !offering.getSessions().isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR,
                    "Cannot modify sessions on a published offering with existing sessions");
        }

        ZoneId teacherZone = timezoneService.parseZone(offering.getTeacherTimezone());
        List<ClassSession> newSessions = new ArrayList<>();

        for (AddSessionsRequest.SessionTimeRequest sessionRequest : request.getSessions()) {
            Instant startAt = timezoneService.toInstant(sessionRequest.getLocalStart(), teacherZone);
            Instant endAt = timezoneService.toInstant(sessionRequest.getLocalEnd(), teacherZone);

            if (!endAt.isAfter(startAt)) {
                throw new AppException(ErrorCode.VALIDATION_ERROR,
                        "Session end time must be after start time");
            }

            ClassSession session = ClassSession.builder()
                    .offering(offering)
                    .teacher(offering.getTeacher())
                    .startAt(startAt)
                    .endAt(endAt)
                    .build();
            newSessions.add(session);
        }

        classSessionRepository.saveAll(newSessions);
        offering.getSessions().addAll(newSessions);

        return offeringResponseMapper.toResponse(offering, teacherZone);
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> listTeacherOfferings(UUID teacherId, boolean upcoming, ZoneId displayZone) {
        userProfileService.requireProfile(teacherId, Role.TEACHER);
        return offeringRepository.findByTeacherWithSessions(teacherId, upcoming).stream()
                .map(offering -> offeringResponseMapper.toResponse(offering, displayZone))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> listAvailableOfferings(boolean upcoming, ZoneId displayZone) {
        return offeringRepository.findPublishedWithSessions(OfferingStatus.PUBLISHED, upcoming).stream()
                .map(offering -> offeringResponseMapper.toResponse(offering, displayZone))
                .toList();
    }

    @Transactional(readOnly = true)
    public Offering getOfferingForBooking(UUID offeringId) {
        return offeringRepository.findById(offeringId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Offering not found"));
    }
}
