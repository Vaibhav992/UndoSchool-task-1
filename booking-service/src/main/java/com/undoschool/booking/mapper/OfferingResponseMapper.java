package com.undoschool.booking.mapper;

import com.undoschool.booking.domain.Offering;
import com.undoschool.booking.dto.OfferingResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;

@Mapper(componentModel = "spring")
public abstract class OfferingResponseMapper {

    @Autowired
    protected SessionResponseMapper sessionResponseMapper;

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "teacherId", source = "teacher.id")
    @Mapping(target = "sessions", ignore = true)
    public abstract OfferingResponse toResponse(Offering offering, @Context ZoneId displayZone);

    @AfterMapping
    protected void mapSessions(Offering offering, @Context ZoneId displayZone,
                               @MappingTarget OfferingResponse.OfferingResponseBuilder builder) {
        builder.sessions(sessionResponseMapper.toResponseList(offering.getSessions(), displayZone));
    }
}
