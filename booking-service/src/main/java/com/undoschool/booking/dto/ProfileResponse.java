package com.undoschool.booking.dto;

import com.undoschool.booking.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private UUID id;
    private String email;
    private Role role;
    private String timezone;
    private String displayName;
}
