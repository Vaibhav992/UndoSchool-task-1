package com.undoschool.booking.dto;

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
public class BookingResponse {

    private UUID id;
    private UUID offeringId;
    private String offeringName;
    private String courseTitle;
    private UUID parentId;
    private String status;
    private String displayTimezone;
    private List<SessionResponse> sessions;
}
