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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private CourseMapper courseMapper;
    @Mock private ActivityLogService activityLogService;

    @InjectMocks
    private CourseService courseService;

    @Test
    void create_success() {
        CreateCourseRequest request = new CreateCourseRequest(
                "CS101", "Intro to CS", "Fundamentals", (short) 3, "CS");

        when(courseRepository.findByCode("CS101")).thenReturn(Optional.empty());

        when(courseRepository.save(any())).thenAnswer(invocation -> {
            Course c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        CourseResponse expected = new CourseResponse(1L, "CS101", "Intro to CS",
                "Fundamentals", (short) 3, "CS", true, null, null);
        when(courseMapper.toResponse(any())).thenReturn(expected);

        CourseResponse result = courseService.create(request);

        assertThat(result).isEqualTo(expected);
        verify(activityLogService).log(eq("COURSE_CREATED"), eq("Course"), eq(1L), any());
    }

    @Test
    void create_duplicateCode_throws() {
        CreateCourseRequest request = new CreateCourseRequest(
                "CS101", "Intro to CS", "Fundamentals", (short) 3, "CS");

        when(courseRepository.findByCode("CS101")).thenReturn(Optional.of(new Course()));

        assertThatThrownBy(() -> courseService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Course code CS101 already exists");
    }

    @Test
    void findById_success() {
        Course course = Course.builder()
                .code("CS101").title("Intro to CS").creditHours((short) 3).isActive(true).build();
        course.setId(1L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        CourseResponse expected = new CourseResponse(1L, "CS101", "Intro to CS",
                null, (short) 3, null, true, null, null);
        when(courseMapper.toResponse(any())).thenReturn(expected);

        CourseResponse result = courseService.findById(1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void findById_notFound_throws() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void findAll_returnsList() {
        Course c1 = Course.builder().code("CS101").build();
        c1.setId(1L);
        Course c2 = Course.builder().code("CS201").build();
        c2.setId(2L);

        when(courseRepository.findAll()).thenReturn(List.of(c1, c2));
        when(courseMapper.toResponse(any())).thenReturn(
                new CourseResponse(1L, "CS101", null, null, null, null, true, null, null),
                new CourseResponse(2L, "CS201", null, null, null, null, true, null, null));

        List<CourseResponse> results = courseService.findAll();

        assertThat(results).hasSize(2);
    }

    @Test
    void update_partialFields() {
        Course course = Course.builder()
                .code("CS101").title("Intro to CS")
                .description("Old desc").creditHours((short) 3)
                .department("CS").isActive(true).build();
        course.setId(1L);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        UpdateCourseRequest request = new UpdateCourseRequest(
                "Updated Title", "Updated desc", (short) 4, "CS", true);

        when(courseMapper.toResponse(any())).thenReturn(
                new CourseResponse(1L, "CS101", "Updated Title",
                        "Updated desc", (short) 4, "CS", true, null, null));

        CourseResponse result = courseService.update(1L, request);

        assertThat(result.title()).isEqualTo("Updated Title");
        assertThat(course.getTitle()).isEqualTo("Updated Title");
        assertThat(course.getCreditHours()).isEqualTo((short) 4);
        verify(activityLogService).log(eq("COURSE_UPDATED"), eq("Course"), eq(1L), any());
    }


    @Test
    void delete_notFound_throws() {
        when(courseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }
}
