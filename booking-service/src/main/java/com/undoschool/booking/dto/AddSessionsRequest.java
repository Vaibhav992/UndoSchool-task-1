package com.undoschool.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSessionsRequest {

    @NotEmpty
    @Valid
    private List<SessionTimeRequest> sessions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionTimeRequest {

        @NotNull
        private LocalDateTime localStart;

        @NotNull
        private LocalDateTime localEnd;
    }
}
