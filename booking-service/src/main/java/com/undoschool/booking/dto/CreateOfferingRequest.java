package com.undoschool.booking.dto;

import com.undoschool.booking.domain.OfferingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOfferingRequest {

    @NotNull
    private UUID courseId;

    @NotBlank
    private String name;

    @NotBlank
    private String teacherTimezone;

    private OfferingStatus status;
}
