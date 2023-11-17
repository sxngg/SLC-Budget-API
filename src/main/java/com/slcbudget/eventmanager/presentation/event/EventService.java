package com.slcbudget.eventmanager.presentation.event;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slcbudget.eventmanager.domain.projections.EventContactProjection;
import com.slcbudget.eventmanager.presentation.event_contact.EventContactService;

import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventContactService eventContactService;

    public Map<Long, BigDecimal> getContactBalancesByEvent(Long eventId) {
        System.out.println("id" + eventId);
        List<EventContactProjection> eventContacts = eventContactService.getEventContactsByEventId(eventId);
        System.out.println("event contacts" + eventContacts);
        return calculateContactBalances(eventContacts);
    }

    private Map<Long, BigDecimal> calculateContactBalances(List<EventContactProjection> eventContacts) {
        Map<Long, BigDecimal> contactBalances = new HashMap<>();
        System.out.println("event contacts 2" + eventContacts);

        for (EventContactProjection eventContact : eventContacts) {
            Long contactId = eventContact.getEvent_contact_id();
            BigDecimal currentBalance = contactBalances.getOrDefault(contactId, BigDecimal.ZERO);
            BigDecimal newBalance = currentBalance.add(eventContact.getBalance());
            contactBalances.put(contactId, newBalance);
        }
        System.out.println("contactBalances" + contactBalances);

        return contactBalances;
    }

}
