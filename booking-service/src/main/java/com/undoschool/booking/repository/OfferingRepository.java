package com.undoschool.booking.repository;

import com.undoschool.booking.domain.Offering;
import com.undoschool.booking.domain.OfferingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OfferingRepository extends JpaRepository<Offering, UUID> {

    @Query("""
            SELECT DISTINCT o FROM Offering o
            LEFT JOIN FETCH o.course
            LEFT JOIN FETCH o.sessions s
            WHERE o.teacher.id = :teacherId
            AND (:upcoming = false OR EXISTS (
                SELECT 1 FROM ClassSession cs
                WHERE cs.offering = o AND cs.startAt > CURRENT_TIMESTAMP
            ))
            ORDER BY o.createdAt DESC
            """)
    List<Offering> findByTeacherWithSessions(@Param("teacherId") UUID teacherId,
                                             @Param("upcoming") boolean upcoming);

    @Query("""
            SELECT DISTINCT o FROM Offering o
            LEFT JOIN FETCH o.course c
            LEFT JOIN FETCH o.teacher
            LEFT JOIN FETCH o.sessions s
            WHERE o.status = :status
            AND (:upcoming = false OR EXISTS (
                SELECT 1 FROM ClassSession cs
                WHERE cs.offering = o AND cs.startAt > CURRENT_TIMESTAMP
            ))
            ORDER BY o.createdAt DESC
            """)
    List<Offering> findPublishedWithSessions(@Param("status") OfferingStatus status,
                                             @Param("upcoming") boolean upcoming);
}
