package com.slcbudget.eventmanager.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slcbudget.eventmanager.domain.EventContact;

public interface EventContactRepository extends JpaRepository<EventContact, Long> {

  @Query("SELECT e FROM EventContact e WHERE e.event.event_id = :eventId")
  Page<EventContact> findEventContactByEventId(@Param("eventId") Long eventId, Pageable pageable);
}
