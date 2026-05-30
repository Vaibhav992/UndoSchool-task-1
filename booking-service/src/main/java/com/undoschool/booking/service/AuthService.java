package com.undoschool.booking.service;

import com.undoschool.booking.domain.Role;
import com.undoschool.booking.domain.UserProfile;
import com.undoschool.booking.dto.AuthResponse;
import com.undoschool.booking.dto.LoginRequest;
import com.undoschool.booking.dto.ProfileResponse;
import com.undoschool.booking.dto.RegisterRequest;
import com.undoschool.booking.exception.AppException;
import com.undoschool.booking.exception.ErrorCode;
import com.undoschool.booking.mapper.UserProfileMapper;
import com.undoschool.booking.repository.UserProfileRepository;
import com.undoschool.booking.security.JwtService;
import com.undoschool.booking.security.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TimezoneService timezoneService;
    private final UserProfileMapper userProfileMapper;

    public AuthService(UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       TimezoneService timezoneService,
                       UserProfileMapper userProfileMapper) {
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.timezoneService = timezoneService;
        this.userProfileMapper = userProfileMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        timezoneService.parseZone(request.getTimezone());

        if (userProfileRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.VALIDATION_ERROR, "Email is already registered");
        }

        UserProfile profile = UserProfile.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .timezone(request.getTimezone())
                .displayName(request.getDisplayName())
                .build();

        profile = userProfileRepository.save(profile);
        SecurityUser securityUser = new SecurityUser(profile);
        ProfileResponse profileResponse = userProfileMapper.toResponse(profile);

        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(securityUser))
                .tokenType("Bearer")
                .profile(profileResponse)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        UserProfile profile = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED, "Invalid credentials"));

        SecurityUser securityUser = new SecurityUser(profile);
        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(securityUser))
                .tokenType("Bearer")
                .profile(userProfileMapper.toResponse(profile))
                .build();
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID profileId) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Profile not found"));
        return userProfileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public UserProfile requireProfile(UUID profileId, Role expectedRole) {
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Profile not found"));
        if (profile.getRole() != expectedRole) {
            throw new AppException(ErrorCode.FORBIDDEN, "Invalid role for this operation");
        }
        return profile;
    }
}
