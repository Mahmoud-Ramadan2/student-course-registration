package com.mahmoudramadan.studentregistration.term.service;

import com.mahmoudramadan.studentregistration.activity.service.ActivityLogService;
import com.mahmoudramadan.studentregistration.shared.exception.BusinessException;
import com.mahmoudramadan.studentregistration.shared.exception.ResourceNotFoundException;
import com.mahmoudramadan.studentregistration.term.dto.CreateTermRequest;
import com.mahmoudramadan.studentregistration.term.dto.TermResponse;
import com.mahmoudramadan.studentregistration.term.dto.UpdateTermRequest;
import com.mahmoudramadan.studentregistration.term.entity.Term;
import com.mahmoudramadan.studentregistration.term.mapper.TermMapper;
import com.mahmoudramadan.studentregistration.term.repo.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;
    private final TermMapper termMapper;
    private final ActivityLogService activityLogService;

    @Transactional
    public TermResponse create(CreateTermRequest request) {
        if (termRepository.findByName(request.name()).isPresent()) {
            throw new BusinessException("Term name " + request.name() + " already exists");
        }

        Term term = Term.builder()
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .registrationStart(request.registrationStart())
                .registrationEnd(request.registrationEnd())
                .isActive(request.active())
                .build();
        termRepository.save(term);

        activityLogService.log("TERM_CREATED", "Term", term.getId(),
                java.util.Map.of("name", term.getName()));

        return termMapper.toResponse(term);
    }

    @Transactional(readOnly = true)
    public TermResponse findById(Long id) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));
        return termMapper.toResponse(term);
    }

    @Transactional(readOnly = true)
    public List<TermResponse> findAll() {
        return termRepository.findAll().stream()
                .map(termMapper::toResponse)
                .toList();
    }

    @Transactional
    public TermResponse update(Long id, UpdateTermRequest request) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));

        if (request.name() != null && !request.name().equals(term.getName())) {
            if (termRepository.findByName(request.name()).isPresent()) {
                throw new BusinessException("Term name " + request.name() + " already exists");
            }
            term.setName(request.name());
        }
        if (request.startDate() != null && !request.startDate().equals(term.getStartDate())) {
            term.setStartDate(request.startDate());
        }
        if (request.endDate() != null && !request.endDate().equals(term.getEndDate())) {
            term.setEndDate(request.endDate());
        }
        if (request.registrationStart() != null && !request.registrationStart().equals(term.getRegistrationStart())) {
            term.setRegistrationStart(request.registrationStart());
        }
        if (request.registrationEnd() != null && !request.registrationEnd().equals(term.getRegistrationEnd())) {
            term.setRegistrationEnd(request.registrationEnd());
        }
        if (request.active() != null && !request.active().equals(term.isActive())) {
            term.setActive(request.active());
        }

        termRepository.save(term);

        activityLogService.log("TERM_UPDATED", "Term", id,
                java.util.Map.of("name", term.getName()));

        return termMapper.toResponse(term);
    }

    @Transactional
    public void delete(Long id) {
        Term term = termRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Term not found"));
        termRepository.delete(term);

        activityLogService.log("TERM_DELETED", "Term", id,
                java.util.Map.of("name", term.getName()));
    }
}
