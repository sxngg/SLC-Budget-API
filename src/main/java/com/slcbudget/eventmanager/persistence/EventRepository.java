package com.slcbudget.eventmanager.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slcbudget.eventmanager.domain.Event;
import com.slcbudget.eventmanager.domain.projections.EventProjection;

public interface EventRepository extends JpaRepository<Event, Long> {
  
  @Query("SELECT " + 
    "e.event_id as event_id," +
    "e.name as name," +
    "e.description as description, "+ 
    "e.type as type, "+ 
    "e.picture as picture, "+ 
    "e.owner.id as owner_id "+ 
    "FROM Event e " +
    "WHERE e.owner.id = :userId")
  Page<EventProjection> findEventsByOwnerId(@Param("userId") Long userId, Pageable pageable);

  Page<Event> findByOwnerId(Long userId, Pageable pageable);

}
