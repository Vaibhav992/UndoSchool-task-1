package com.undoschool.booking.repository;

import com.undoschool.booking.domain.Booking;
import com.undoschool.booking.domain.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByParentIdAndOfferingIdAndStatus(UUID parentId, UUID offeringId, BookingStatus status);

    @Query("""
            SELECT DISTINCT b FROM Booking b
            JOIN FETCH b.offering o
            JOIN FETCH o.course
            LEFT JOIN FETCH o.sessions s
            WHERE b.parent.id = :parentId
            AND b.status = :status
            AND (:upcoming = false OR EXISTS (
                SELECT 1 FROM ClassSession cs
                WHERE cs.offering = o AND cs.startAt > CURRENT_TIMESTAMP
            ))
            ORDER BY b.createdAt DESC
            """)
    List<Booking> findByParentWithDetails(@Param("parentId") UUID parentId,
                                          @Param("status") BookingStatus status,
                                          @Param("upcoming") boolean upcoming);
}
