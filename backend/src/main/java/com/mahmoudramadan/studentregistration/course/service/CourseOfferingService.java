package com.mahmoudramadan.studentregistration.course.service;

import com.mahmoudramadan.studentregistration.activity.service.ActivityLogService;
import com.mahmoudramadan.studentregistration.course.dto.CourseOfferingResponse;
import com.mahmoudramadan.studentregistration.course.dto.CreateCourseOfferingRequest;
import com.mahmoudramadan.studentregistration.course.dto.UpdateCourseOfferingRequest;
import com.mahmoudramadan.studentregistration.course.entity.Course;
import com.mahmoudramadan.studentregistration.course.entity.CourseOffering;
import com.mahmoudramadan.studentregistration.course.enums.OfferingStatus;
import com.mahmoudramadan.studentregistration.course.mapper.CourseOfferingMapper;
import com.mahmoudramadan.studentregistration.course.repo.CourseOfferingRepository;
import com.mahmoudramadan.studentregistration.course.repo.CourseRepository;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import com.mahmoudramadan.studentregistration.staff.entity.Staff;
import com.mahmoudramadan.studentregistration.staff.repo.StaffRepository;
import com.mahmoudramadan.studentregistration.term.entity.Term;
import com.mahmoudramadan.studentregistration.term.repo.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CourseOfferingService {

    private final CourseOfferingRepository courseOfferingRepository;
    private final CourseRepository courseRepository;
    private final TermRepository termRepository;
    private final StaffRepository staffRepository;
    private final CourseOfferingMapper courseOfferingMapper;
    private final ActivityLogService activityLogService;

    @Transactional
    public CourseOfferingResponse create(CreateCourseOfferingRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        Term term = termRepository.findById(request.termId())
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        Staff instructor = null;
        if (request.instructorId() != null) {
            instructor = staffRepository.findById(request.instructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
        }

        if (request.sectionNumber() != null && courseOfferingRepository
                .findByCourseIdAndTermIdAndSectionNumber(request.courseId(), request.termId(), request.sectionNumber())
                .isPresent()) {
            throw new BusinessException("Section " + request.sectionNumber() + " already exists for this course and term");
        }

        CourseOffering offering = CourseOffering.builder()
                .course(course)
                .term(term)
                .sectionNumber(request.sectionNumber())
                .instructor(instructor)
                .capacity(request.capacity())
                .waitlistCapacity(request.waitlistCapacity() != null ? request.waitlistCapacity() : 0)
                .room(request.room())
                .daysOfWeek(request.daysOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(OfferingStatus.SCHEDULED)
                .build();
        courseOfferingRepository.save(offering);

        activityLogService.log("OFFERING_CREATED", "CourseOffering", offering.getId(),
              Map.of("courseCode", course.getCode(), "section", offering.getSectionNumber()));

        return courseOfferingMapper.toResponse(offering);
    }

    @Transactional(readOnly = true)
    public CourseOfferingResponse findById(Long id) {
        CourseOffering offering = courseOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));
        return courseOfferingMapper.toResponse(offering);
    }

    @Transactional(readOnly = true)
    public List<CourseOfferingResponse> findAll() {
        return courseOfferingRepository.findAll().stream()
                .map(courseOfferingMapper::toResponse)
                .toList();
    }

    @Transactional
    public CourseOfferingResponse update(Long id, UpdateCourseOfferingRequest request) {
        CourseOffering offering = courseOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));

        if (request.sectionNumber() != null && !request.sectionNumber().equals(offering.getSectionNumber())) {
            if (courseOfferingRepository.findByCourseIdAndTermIdAndSectionNumber(
                    offering.getCourse().getId(), offering.getTerm().getId(), request.sectionNumber()).isPresent()) {
                throw new BusinessException("Section " + request.sectionNumber() + " already exists for this course and term");
            }
            offering.setSectionNumber(request.sectionNumber());
        }
        if (request.instructorId() != null) {
            Long currentInstructorId = offering.getInstructor() != null ? offering.getInstructor().getId() : null;
            if (!request.instructorId().equals(currentInstructorId)) {
                Staff instructor = staffRepository.findById(request.instructorId())
                        .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));
                offering.setInstructor(instructor);
            }
        }
        if (request.capacity() != null && !request.capacity().equals(offering.getCapacity())) {
            offering.setCapacity(request.capacity());
        }
        if (request.waitlistCapacity() != null && !request.waitlistCapacity().equals(offering.getWaitlistCapacity())) {
            offering.setWaitlistCapacity(request.waitlistCapacity());
        }
        if (request.room() != null && !request.room().equals(offering.getRoom())) {
            offering.setRoom(request.room());
        }
        if (request.daysOfWeek() != null && !request.daysOfWeek().equals(offering.getDaysOfWeek())) {
            offering.setDaysOfWeek(request.daysOfWeek());
        }
        if (request.startTime() != null && !request.startTime().equals(offering.getStartTime())) {
            offering.setStartTime(request.startTime());
        }
        if (request.endTime() != null && !request.endTime().equals(offering.getEndTime())) {
            offering.setEndTime(request.endTime());
        }
        if (request.status() != null && !request.status().equals(offering.getStatus())) {
            offering.setStatus(request.status());
        }

        courseOfferingRepository.save(offering);

        activityLogService.log("OFFERING_UPDATED", "CourseOffering", id,
               Map.of("courseCode", offering.getCourse().getCode()));

        return courseOfferingMapper.toResponse(offering);
    }

    @Transactional(readOnly = true)
    public boolean isInstructorOfOffering(Long offeringId, Long staffUserId) {
        return courseOfferingRepository.isInstructorOfOffering(offeringId, staffUserId);
    }

    @Transactional
    public void delete(Long id) {
        CourseOffering offering = courseOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));


        courseOfferingRepository.delete(offering);

        activityLogService.log("OFFERING_DELETED", "CourseOffering", id,
                Map.of("courseCode", offering.getCourse().getCode()));
    }
}
