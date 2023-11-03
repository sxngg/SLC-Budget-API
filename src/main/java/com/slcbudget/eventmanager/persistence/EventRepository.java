package com.slcbudget.eventmanager.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.slcbudget.eventmanager.domain.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
  
  Page<Event> findByOwnerId(Long userId, Pageable pageable);

}
