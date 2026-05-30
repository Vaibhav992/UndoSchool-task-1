package com.undoschool.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private UUID id;
    private UUID offeringId;
    private UUID teacherId;
    private String startAtUtc;
    private String endAtUtc;
    private String startAtLocal;
    private String endAtLocal;
    private String displayTimezone;
}
