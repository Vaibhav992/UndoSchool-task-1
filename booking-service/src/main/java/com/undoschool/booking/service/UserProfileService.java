package com.undoschool.booking.service;

import com.undoschool.booking.domain.Role;
import com.undoschool.booking.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserProfileService {

    private final AuthService authService;

    public UserProfileService(AuthService authService) {
        this.authService = authService;
    }

    public UserProfile requireProfile(UUID profileId, Role expectedRole) {
        return authService.requireProfile(profileId, expectedRole);
    }
}
