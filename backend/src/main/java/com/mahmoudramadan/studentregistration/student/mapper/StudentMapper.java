package com.mahmoudramadan.studentregistration.student.mapper;

import com.mahmoudramadan.studentregistration.student.dto.StudentResponse;
import com.mahmoudramadan.studentregistration.student.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "createdAt", source = "user.createdAt")
    @Mapping(target = "updatedAt", source = "user.updatedAt")
    StudentResponse toResponse(Student student);
}
