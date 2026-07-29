package com.mahmoudramadan.studentregistration.course.mapper;

import com.mahmoudramadan.studentregistration.course.dto.CourseOfferingResponse;
import com.mahmoudramadan.studentregistration.course.entity.CourseOffering;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseOfferingMapper {

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseCode", source = "course.code")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "termName", source = "term.name")
    @Mapping(target = "instructorId", source = "instructor.id")
    @Mapping(target = "instructorName", expression = "java(mapInstructorName(offering))")
    CourseOfferingResponse toResponse(CourseOffering offering);

    default String mapInstructorName(CourseOffering offering) {
        if (offering.getInstructor() == null) return null;
        return offering.getInstructor().getFirstName() + " " + offering.getInstructor().getLastName();
    }
}
