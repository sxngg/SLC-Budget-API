package com.slcbudget.eventmanager.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slcbudget.eventmanager.domain.EventContact;
import com.slcbudget.eventmanager.domain.projections.EventContactProjection;
import com.slcbudget.eventmanager.domain.projections.EventInfoProjection;

import java.util.List;

public interface EventContactRepository extends JpaRepository<EventContact, Long> {

  @Query("SELECT " +
  "ec.event_contact_id as event_contact_id, " + 
  "ec.contact.id as contactId, " + 
  "ec.contact.email as contactEmail, " + 
  "ec.contact.name as contactName, " + 
  "ec.contact.lastName as contactLastName, " + 
  "ec.contact.username as contactUsername, " + 
  "ec.contact.profileImage as contactProfileImage, " + 
  "ec.balance as balance " +
  "FROM EventContact ec " + 
  "WHERE ec.event.id = :eventId")
  Page<EventContactProjection> findEventContactByEventId(@Param("eventId") Long eventId, Pageable pageable);

  @Query("SELECT ec FROM EventContact ec WHERE ec.event.event_id = :eventId")
  List<EventContactProjection> findEventContactByEventId(Long eventId);

  @Query("SELECT ec FROM EventContact ec WHERE ec.event.event_id = :eventId")
  List<EventContact> findEventContactWithoutProjectionByEventId(Long eventId);

  @Query("SELECT " + 
  "ec.event.event_id as event_id, " + 
  "ec.event.name as name, " +
  "ec.event.description as description, " + 
  "ec.event.type as type, " + 
  "ec.event.picture as picture, " + 
  "ec.event.owner.id as owner_id, " + 
  "ec.event.owner.email as ownerEmail, " + 
  "ec.event.owner.name as ownerName, " + 
  "ec.event.owner.username as ownerUsername, " + 
  "ec.event.owner.profileImage as ownerProfileImage " + 
  "FROM EventContact ec WHERE ec.contact.id = :contactId")
  Page<EventInfoProjection> findEventsByContactId(@Param("contactId") Long contactId, Pageable pageable);

  @Query("SELECT ec FROM EventContact ec WHERE ec.contact.id = :contactId AND ec.event.event_id = :eventId")
  EventContact findByContactIdAndEventId(@Param("contactId") Long contactId, @Param("eventId") Long eventId);
}
