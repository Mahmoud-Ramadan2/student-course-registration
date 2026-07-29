package com.mahmoudramadan.studentregistration.term.repo;

import com.mahmoudramadan.studentregistration.term.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {
    java.util.Optional<Term> findByName(String name);
}
