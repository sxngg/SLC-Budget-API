package com.slcbudget.eventmanager.domain.dto;

import com.slcbudget.eventmanager.domain.TypeEvent;

public record EventResponseDTO(
    Long event_id,
    String name,
    String description,
    TypeEvent type,
    Long owner_id,
    String imageUrl
) {
    public EventResponseDTO(Long eventId, EventDataDTO eventDataDTO, String imageUrl) {
        this(eventId, eventDataDTO.name(), eventDataDTO.description(), eventDataDTO.type(), eventDataDTO.owner_id(), imageUrl);
    }

    public EventResponseDTO(Long eventId, EventDataEditDTO eventDataEditDTO, String imageUrl, Long owner_id) {
        this(eventId, eventDataEditDTO.name(), eventDataEditDTO.description(), eventDataEditDTO.type(), owner_id, imageUrl);
    }

}
