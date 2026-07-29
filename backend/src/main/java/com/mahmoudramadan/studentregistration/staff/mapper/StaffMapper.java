package com.mahmoudramadan.studentregistration.staff.mapper;

import com.mahmoudramadan.studentregistration.staff.dto.StaffResponse;
import com.mahmoudramadan.studentregistration.staff.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StaffMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "updatedAt", source = "user.updatedAt")
    StaffResponse toResponse(Staff staff);
}
