package com.mahmoudramadan.studentregistration.course.service;

import com.mahmoudramadan.studentregistration.activity.service.ActivityLogService;
import com.mahmoudramadan.studentregistration.course.dto.CreateCourseRequest;
import com.mahmoudramadan.studentregistration.course.dto.CourseResponse;
import com.mahmoudramadan.studentregistration.course.dto.UpdateCourseRequest;
import com.mahmoudramadan.studentregistration.course.entity.Course;
import com.mahmoudramadan.studentregistration.course.mapper.CourseMapper;
import com.mahmoudramadan.studentregistration.course.repo.CourseRepository;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final ActivityLogService activityLogService;

    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        if (courseRepository.findByCode(request.code()).isPresent()) {
            throw new BusinessException("Course code " + request.code() + " already exists");
        }

        Course course = Course.builder()
                .code(request.code())
                .title(request.title())
                .description(request.description())
                .creditHours(request.creditHours())
                .department(request.department())
                .isActive(true)
                .build();
        courseRepository.save(course);

        activityLogService.log("COURSE_CREATED", "Course", course.getId(),
                Map.of("code", course.getCode(), "title", course.getTitle()));

        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return courseMapper.toResponse(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toResponse)
                .toList();
    }

    @Transactional
    public CourseResponse update(Long id, UpdateCourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (request.title() != null && !request.title().equals(course.getTitle())) {
            course.setTitle(request.title());
        }
        if (request.description() != null && !request.description().equals(course.getDescription())) {
            course.setDescription(request.description());
        }
        if (request.creditHours() != null && !request.creditHours().equals(course.getCreditHours())) {
            course.setCreditHours(request.creditHours());
        }
        if (request.department() != null && !request.department().equals(course.getDepartment())) {
            course.setDepartment(request.department());
        }
        if (request.active() != null && !request.active().equals(course.isActive())) {
            course.setActive(request.active());
        }

        courseRepository.save(course);

        activityLogService.log("COURSE_UPDATED", "Course", id,
                Map.of("code", course.getCode()));

        return courseMapper.toResponse(course);
    }

    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        courseRepository.delete(course);

        activityLogService.log("COURSE_DELETED", "Course", id,
                Map.of("code", course.getCode()));
    }
}
