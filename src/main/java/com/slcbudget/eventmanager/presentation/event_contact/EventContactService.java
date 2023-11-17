package com.slcbudget.eventmanager.presentation.event_contact;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.slcbudget.eventmanager.domain.projections.EventContactProjection;
import com.slcbudget.eventmanager.persistence.EventContactRepository;

@Service
public class EventContactService {

    @Autowired
    private EventContactRepository eventContactRepository;

    public List<EventContactProjection> getEventContactsByEventId(Long eventId) {
        System.out.println("service event contact" + eventId);
        return eventContactRepository.findEventContactByEventId(eventId);
    }
}
