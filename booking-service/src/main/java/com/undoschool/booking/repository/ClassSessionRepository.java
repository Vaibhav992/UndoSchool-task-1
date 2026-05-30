package com.undoschool.booking.repository;

import com.undoschool.booking.domain.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {

    List<ClassSession> findByOfferingIdOrderByStartAtAsc(UUID offeringId);
}
