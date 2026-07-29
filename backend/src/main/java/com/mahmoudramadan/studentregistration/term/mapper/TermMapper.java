package com.mahmoudramadan.studentregistration.term.mapper;

import com.mahmoudramadan.studentregistration.term.dto.TermResponse;
import com.mahmoudramadan.studentregistration.term.entity.Term;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TermMapper {

    TermResponse toResponse(Term term);
}
