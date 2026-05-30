package com.undoschool.booking.security;

import com.undoschool.booking.exception.AppException;
import com.undoschool.booking.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {

    public SecurityUser requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser user)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Authentication required");
        }
        return user;
    }

    public UUID requireProfileId() {
        return requireAuthenticatedUser().getProfileId();
    }
}
