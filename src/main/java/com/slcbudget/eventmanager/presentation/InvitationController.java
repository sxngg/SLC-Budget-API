package com.slcbudget.eventmanager.presentation;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.slcbudget.eventmanager.domain.Event;
import com.slcbudget.eventmanager.domain.EventContact;
import com.slcbudget.eventmanager.domain.Invitation;
import com.slcbudget.eventmanager.domain.InvitationState;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.domain.dto.InvitationCreateDTO;
import com.slcbudget.eventmanager.persistence.EventContactRepository;
import com.slcbudget.eventmanager.persistence.EventRepository;
import com.slcbudget.eventmanager.persistence.InvitationRepository;
import com.slcbudget.eventmanager.persistence.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/invitation")
public class InvitationController {

  @Autowired
  private InvitationRepository invitationRepository;

  @Autowired
  private EventContactRepository eventContactRepository;
  
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EventRepository eventRepository;

  @GetMapping("{id}")
  public ResponseEntity<Invitation> getInvitationById(@PathVariable Long id) {
    Optional<Invitation> optionalInvitation = invitationRepository.findById(id);

    if (optionalInvitation.isPresent()) {
      Invitation invitation = optionalInvitation.get();
      return ResponseEntity.ok().body(invitation);
    }
    return ResponseEntity.badRequest().build();
  }

  @GetMapping("/by-event/{event_id}")
  public ResponseEntity<Page<Invitation>> getInvitationByEventId(@PathVariable Long event_id,
      @PageableDefault(size = 3) Pageable pagination) {
        
    Page<Invitation> invitations = invitationRepository.findInvitationsByEventId(event_id, pagination);
    return ResponseEntity.ok(invitations);
  }

  @PostMapping("/create")
  public ResponseEntity<Invitation> createInvitation(
      @Valid @RequestBody InvitationCreateDTO invitationCreateDTO,
      UriComponentsBuilder uriComponentsBuilder) {

    Optional<UserEntity> contactOptional = userRepository.findById(invitationCreateDTO.contactId());
    Optional<Event> eventOptional = eventRepository.findById(invitationCreateDTO.eventId());

    if (contactOptional.isPresent() && eventOptional.isPresent()) {
      UserEntity contact = contactOptional.get();
      Event event = eventOptional.get();

      Invitation invitation = invitationRepository
          .save(new Invitation(contact, event, InvitationState.PENDING));

      URI url = uriComponentsBuilder.path("/invitation/{id}")
          .buildAndExpand(invitation.getInvitation_id()).toUri();

      return ResponseEntity.created(url).body(invitation);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/update/{invitation_id}")
  public ResponseEntity<Invitation> acceptInvitation(@PathVariable Long invitation_id,
      @RequestParam InvitationState invitationState) {

    Optional<Invitation> optionalInvitation = invitationRepository.findById(invitation_id);

    if (optionalInvitation.isPresent()) {
      Invitation invitation = optionalInvitation.get();

      switch (invitationState) {
        case ACCEPTED:
          invitation.setInvitation_state(InvitationState.ACCEPTED);
          invitationRepository.save(invitation);
          EventContact eventContact = new EventContact();
          eventContact.setEvent(invitation.getEvent());
          eventContact.setContact(invitation.getContact());
          eventContactRepository.save(eventContact);
          return ResponseEntity.ok(invitation);

        case REJECTED:
          invitation.setInvitation_state(InvitationState.REJECTED);
          invitationRepository.save(invitation);
          return ResponseEntity.ok(invitation);

        default:
          return ResponseEntity.badRequest().build();
      }
    } else {
      return ResponseEntity.notFound().build();
    }
  }

}
