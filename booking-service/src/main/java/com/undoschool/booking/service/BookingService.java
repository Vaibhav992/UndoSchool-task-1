package com.undoschool.booking.service;

import com.undoschool.booking.domain.Booking;
import com.undoschool.booking.domain.BookingStatus;
import com.undoschool.booking.domain.ClassSession;
import com.undoschool.booking.domain.Offering;
import com.undoschool.booking.domain.OfferingStatus;
import com.undoschool.booking.domain.ParentTimeBlock;
import com.undoschool.booking.domain.Role;
import com.undoschool.booking.domain.UserProfile;
import com.undoschool.booking.dto.BookingResponse;
import com.undoschool.booking.exception.AppException;
import com.undoschool.booking.exception.ErrorCode;
import com.undoschool.booking.mapper.BookingResponseMapper;
import com.undoschool.booking.repository.BookingRepository;
import com.undoschool.booking.repository.ClassSessionRepository;
import com.undoschool.booking.repository.ParentTimeBlockRepository;
import com.undoschool.booking.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ParentTimeBlockRepository parentTimeBlockRepository;
    private final UserProfileRepository userProfileRepository;
    private final OfferingService offeringService;
    private final UserProfileService userProfileService;
    private final BookingResponseMapper bookingResponseMapper;

    public BookingService(BookingRepository bookingRepository,
                          ClassSessionRepository classSessionRepository,
                          ParentTimeBlockRepository parentTimeBlockRepository,
                          UserProfileRepository userProfileRepository,
                          OfferingService offeringService,
                          UserProfileService userProfileService,
                          BookingResponseMapper bookingResponseMapper) {
        this.bookingRepository = bookingRepository;
        this.classSessionRepository = classSessionRepository;
        this.parentTimeBlockRepository = parentTimeBlockRepository;
        this.userProfileRepository = userProfileRepository;
        this.offeringService = offeringService;
        this.userProfileService = userProfileService;
        this.bookingResponseMapper = bookingResponseMapper;
    }

    @Transactional
    public BookingResponse bookOffering(UUID parentId, UUID offeringId, ZoneId displayZone) {
        userProfileService.requireProfile(parentId, Role.PARENT);
        userProfileRepository.findByIdForUpdate(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Parent not found"));

        Offering offering = offeringService.getOfferingForBooking(offeringId);

        if (offering.getStatus() != OfferingStatus.PUBLISHED) {
            throw new AppException(ErrorCode.OFFERING_NOT_BOOKABLE, "Offering is not published");
        }

        List<ClassSession> sessions = classSessionRepository.findByOfferingIdOrderByStartAtAsc(offeringId);
        if (sessions.isEmpty()) {
            throw new AppException(ErrorCode.OFFERING_NOT_BOOKABLE, "Offering has no sessions");
        }

        bookingRepository.findByParentIdAndOfferingIdAndStatus(parentId, offeringId, BookingStatus.CONFIRMED)
                .ifPresent(existing -> {
                    throw new AppException(ErrorCode.ALREADY_BOOKED, "Offering is already booked");
                });

        if (parentTimeBlockRepository.hasOverlapWithOffering(parentId, offeringId)) {
            throw new AppException(ErrorCode.TIME_CONFLICT,
                    "Booking overlaps with an existing session");
        }

        UserProfile parent = userProfileRepository.findById(parentId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Parent not found"));

        Booking booking = Booking.builder()
                .offering(offering)
                .parent(parent)
                .status(BookingStatus.CONFIRMED)
                .build();
        booking = bookingRepository.save(booking);

        List<ParentTimeBlock> blocks = new ArrayList<>();
        for (ClassSession session : sessions) {
            blocks.add(ParentTimeBlock.builder()
                    .parent(parent)
                    .booking(booking)
                    .session(session)
                    .startAt(session.getStartAt())
                    .endAt(session.getEndAt())
                    .build());
        }
        parentTimeBlockRepository.saveAll(blocks);
        offering.getSessions().clear();
        offering.getSessions().addAll(sessions);

        return bookingResponseMapper.toResponse(booking, displayZone);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsForParent(UUID parentId, boolean upcoming, ZoneId displayZone) {
        userProfileService.requireProfile(parentId, Role.PARENT);
        return bookingRepository.findByParentWithDetails(parentId, BookingStatus.CONFIRMED, upcoming).stream()
                .map(booking -> bookingResponseMapper.toResponse(booking, displayZone))
                .toList();
    }
}
