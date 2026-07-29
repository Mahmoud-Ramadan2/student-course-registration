package com.mahmoudramadan.studentregistration.course.mapper;

import com.mahmoudramadan.studentregistration.course.dto.CourseResponse;
import com.mahmoudramadan.studentregistration.course.entity.Course;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CourseMapper {

    CourseResponse toResponse(Course course);
}
