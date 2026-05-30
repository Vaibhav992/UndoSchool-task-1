package com.undoschool.booking.security;

import com.undoschool.booking.domain.Role;
import com.undoschool.booking.domain.UserProfile;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
public class SecurityUser implements UserDetails {

    private final UUID profileId;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final String timezone;
    private final String displayName;
    private final List<GrantedAuthority> authorities;

    public SecurityUser(UserProfile profile) {
        this.profileId = profile.getId();
        this.email = profile.getEmail();
        this.passwordHash = profile.getPasswordHash();
        this.role = profile.getRole();
        this.timezone = profile.getTimezone();
        this.displayName = profile.getDisplayName();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + profile.getRole().name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
