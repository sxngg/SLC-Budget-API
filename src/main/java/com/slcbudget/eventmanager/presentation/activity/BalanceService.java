package com.slcbudget.eventmanager.presentation.activity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slcbudget.eventmanager.domain.Activity;
import com.slcbudget.eventmanager.domain.ActivityParticipants;
import com.slcbudget.eventmanager.domain.Event;
import com.slcbudget.eventmanager.domain.EventContact;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.persistence.ActivityParticipantsRepository;
import com.slcbudget.eventmanager.persistence.EventContactRepository;
import com.slcbudget.eventmanager.persistence.EventRepository;
import com.slcbudget.eventmanager.persistence.UserRepository;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class BalanceService {

  @Autowired
  private ActivityParticipantsRepository activityParticipantsRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private EventContactRepository eventContactRepository;

  public void updateActivityParticipantsBalances(ActivityParticipants activityParticipant,
      Activity activity, BigDecimal staticValue, BigDecimal percentage, UserEntity participant) {
    activityParticipant.setActivity(activity);
    activityParticipant.setBalance(staticValue);
    activityParticipant.setParticipant(participant);
    activityParticipant.setParticipationPercent(percentage);
    activityParticipant.setStaticValueParticipation(staticValue);
    activityParticipantsRepository.save(activityParticipant);
  }

  public void updateEventBalance(Activity activity) {
    Event event = activity.getEvent();
    BigDecimal currentBalance = event.getEventBalance();
    BigDecimal activityValue = activity.getValue();
    BigDecimal newBalance = currentBalance.add(activityValue);
    event.setEventBalance(newBalance);
    eventRepository.save(event);
  }

  public void updateUserBalances(UserEntity participant, BigDecimal staticValue) {
    System.out.println("user" + participant);
    BigDecimal currentBalance = participant.getBalance();
    System.out.println("balance participant current" + currentBalance);
    BigDecimal newBalance = currentBalance.add(staticValue);
    System.out.println("balance participant new" + newBalance);
    participant.setBalance(newBalance);
    userRepository.save(participant);
    System.out.println("NEW BALANCE participant" + participant.getBalance());
  }

  public void updateEventContactBalance(Long eventId, Long participantId, BigDecimal staticValue) {
    List<EventContact> eventContacts = eventContactRepository.findEventContactWithoutProjectionByEventId(eventId);

    Optional<EventContact> eventContactOptional = eventContacts.stream()
        .filter(contact -> contact.getContact().getId() == participantId)
        .findFirst();

    if (eventContactOptional.isPresent()) {
      EventContact eventContact = eventContactOptional.get();
      BigDecimal currentBalance = eventContact.getBalance();
      eventContact.setBalance(currentBalance.add(staticValue));
      eventContactRepository.save(eventContact);
    }
  }
}
