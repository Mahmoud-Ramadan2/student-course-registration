package com.mahmoudramadan.studentregistration.activity.repo;

import com.mahmoudramadan.studentregistration.activity.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ActivityLog> findByEntityType(String entityType, Pageable pageable);

    Page<ActivityLog> findByAction(String action, Pageable pageable);

    Page<ActivityLog> findByActor_Id(Long actorId, Pageable pageable);
}
