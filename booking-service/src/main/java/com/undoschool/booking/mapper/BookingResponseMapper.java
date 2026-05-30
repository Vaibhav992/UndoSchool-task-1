package com.undoschool.booking.mapper;

import com.undoschool.booking.domain.Booking;
import com.undoschool.booking.domain.Offering;
import com.undoschool.booking.dto.BookingResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;

@Mapper(componentModel = "spring")
public abstract class BookingResponseMapper {

    @Autowired
    protected SessionResponseMapper sessionResponseMapper;

    @Mapping(target = "offeringId", source = "offering.id")
    @Mapping(target = "offeringName", source = "offering.name")
    @Mapping(target = "courseTitle", source = "offering.course.title")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "status", expression = "java(booking.getStatus().name())")
    @Mapping(target = "displayTimezone", expression = "java(displayZone.getId())")
    @Mapping(target = "sessions", ignore = true)
    public abstract BookingResponse toResponse(Booking booking, @Context ZoneId displayZone);

    @AfterMapping
    protected void mapSessions(Booking booking, @Context ZoneId displayZone,
                               @MappingTarget BookingResponse.BookingResponseBuilder builder) {
        Offering offering = booking.getOffering();
        builder.sessions(sessionResponseMapper.toResponseList(offering.getSessions(), displayZone));
    }
}
