package com.undoschool.booking.dto;

import com.undoschool.booking.domain.OfferingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferingResponse {

    private UUID id;
    private UUID courseId;
    private String courseTitle;
    private UUID teacherId;
    private String name;
    private String teacherTimezone;
    private OfferingStatus status;
    private List<SessionResponse> sessions;
}
