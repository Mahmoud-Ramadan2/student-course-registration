package com.mahmoudramadan.studentregistration.shared.dto;

import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;

public record PageRequest(
    @Min(0) int page,
    @Min(1) int size,
    String sortBy,
    String sortDir
) {
    public PageRequest() {
        this(0, 10, "createdAt", "desc");
    }

    public PageRequest toSpringPageRequest() {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return new PageRequest(page, size, sortBy, sortDir);
    }
}
