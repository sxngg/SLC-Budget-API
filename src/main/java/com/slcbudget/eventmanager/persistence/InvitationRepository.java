package com.slcbudget.eventmanager.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slcbudget.eventmanager.domain.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

  @Query("SELECT i FROM Invitation i WHERE i.event.event_id = :eventId")
  Page<Invitation> findInvitationsByEventId(@Param("eventId") Long eventId, Pageable pageable);

}
