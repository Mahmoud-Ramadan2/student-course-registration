package com.mahmoudramadan.studentregistration.enrollment.mapper;

import com.mahmoudramadan.studentregistration.enrollment.dto.EnrollmentResponse;
import com.mahmoudramadan.studentregistration.enrollment.entity.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {

    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(mapStudentName(enrollment))")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "courseCode", source = "course.code")
    @Mapping(target = "courseTitle", source = "course.title")
    @Mapping(target = "offeringId", source = "offering.id")
    @Mapping(target = "sectionNumber", source = "offering.sectionNumber")
    @Mapping(target = "termId", source = "term.id")
    @Mapping(target = "termName", source = "term.name")
    EnrollmentResponse toResponse(Enrollment enrollment);

    default String mapStudentName(Enrollment enrollment) {
        return enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName();
    }
}
