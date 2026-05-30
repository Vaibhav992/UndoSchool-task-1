package com.undoschool.booking.mapper;

import com.undoschool.booking.domain.ClassSession;
import com.undoschool.booking.dto.SessionResponse;
import com.undoschool.booking.service.TimezoneService;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class SessionResponseMapper {

    @Autowired
    protected TimezoneService timezoneService;

    @Mapping(target = "offeringId", source = "offering.id")
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "startAtUtc", expression = "java(timezoneService.formatInstantUtc(session.getStartAt()))")
    @Mapping(target = "endAtUtc", expression = "java(timezoneService.formatInstantUtc(session.getEndAt()))")
    @Mapping(target = "startAtLocal", expression = "java(timezoneService.formatInstantLocal(session.getStartAt(), displayZone))")
    @Mapping(target = "endAtLocal", expression = "java(timezoneService.formatInstantLocal(session.getEndAt(), displayZone))")
    @Mapping(target = "displayTimezone", expression = "java(displayZone.getId())")
    public abstract SessionResponse toResponse(ClassSession session, @Context ZoneId displayZone);

    public List<SessionResponse> toResponseList(List<ClassSession> sessions, ZoneId displayZone) {
        if (sessions == null) {
            return List.of();
        }
        return sessions.stream()
                .sorted(Comparator.comparing(ClassSession::getStartAt))
                .map(session -> toResponse(session, displayZone))
                .toList();
    }
}
