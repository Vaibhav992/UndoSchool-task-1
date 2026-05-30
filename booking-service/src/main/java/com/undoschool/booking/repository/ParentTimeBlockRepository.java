package com.undoschool.booking.repository;

import com.undoschool.booking.domain.ParentTimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ParentTimeBlockRepository extends JpaRepository<ParentTimeBlock, UUID> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM parent_time_blocks ptb
                JOIN sessions s ON s.offering_id = :offeringId
                WHERE ptb.parent_id = :parentId
                  AND ptb.start_at < s.end_at
                  AND s.start_at < ptb.end_at
            )
            """, nativeQuery = true)
    boolean hasOverlapWithOffering(@Param("parentId") UUID parentId,
                                   @Param("offeringId") UUID offeringId);
}
