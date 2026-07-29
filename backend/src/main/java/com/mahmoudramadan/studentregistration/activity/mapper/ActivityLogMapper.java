package com.mahmoudramadan.studentregistration.activity.mapper;

import com.mahmoudramadan.studentregistration.activity.dto.ActivityLogResponse;
import com.mahmoudramadan.studentregistration.activity.entity.ActivityLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    @Mapping(target = "actorId", source = "actor.id")
    @Mapping(target = "actorUsername", source = "actor.username")
    ActivityLogResponse toResponse(ActivityLog log);
}
