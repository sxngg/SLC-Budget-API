package com.slcbudget.eventmanager.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.slcbudget.eventmanager.domain.Activity;
import com.slcbudget.eventmanager.domain.ActivityParticipants;
import com.slcbudget.eventmanager.domain.Event;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.domain.dto.ActivityCreateDTO;
import com.slcbudget.eventmanager.persistence.ActivityParticipantsRepository;
import com.slcbudget.eventmanager.persistence.ActivityRepository;
import com.slcbudget.eventmanager.persistence.EventRepository;
import com.slcbudget.eventmanager.persistence.UserRepository;
import com.slcbudget.eventmanager.utils.Result;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;
    
    @Autowired
    private ActivityParticipantsRepository activityParticipantsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    public Result<Activity> createActivity(ActivityCreateDTO activityDTO) {
      Result<Activity> result = new Result<>();
      try {

        Event event = eventRepository.findById(activityDTO.eventId())
                    .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        Activity activity = new Activity();
        activity.setDescription(activityDTO.description());
        activity.setValue(activityDTO.value());
        activity.setEvent(event);

        activityRepository.save(activity);

        Map<Long, ActivityCreateDTO.ParticipationData> participationData = activityDTO.participationData();
        BigDecimal totalStaticValues = BigDecimal.ZERO;
        BigDecimal totalPercent = BigDecimal.ZERO;

        for (Map.Entry<Long, ActivityCreateDTO.ParticipationData> entry : participationData.entrySet()) {
            Long participantId = entry.getKey();
            ActivityCreateDTO.ParticipationData participantData = entry.getValue();

            UserEntity participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + participantId));
            
            ActivityParticipants activityParticipant = new ActivityParticipants();
            activityParticipant.setActivity(activity);
            activityParticipant.setParticipant(participant);
            activityParticipant.setParticipationPercent(participantData.participationPercentage());
            activityParticipant.setStaticValueParticipation(participantData.staticValue());

            activityParticipantsRepository.save(activityParticipant);

            totalStaticValues = totalStaticValues.add(participantData.staticValue());
            totalPercent = totalPercent.add(participantData.participationPercentage());
        }

        if (totalStaticValues.compareTo(activity.getValue()) > 0
          || totalPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
          result.setSuccess(false);
          result.setError("La sumatoria de los valores o porcentajes excede el valor total de la actividad. ");
          throw new RuntimeException("La sumatoria de los porcentajes excede el 100%.");
        }

        activity.setParticipants(activityParticipantsRepository.findByActivity(activity));

        result.setSuccess(true);
        result.setData(activity);
      } catch (Exception e) {
        result.setSuccess(false);
        result.setError("Error al crear la actividad: " + e.getMessage());
      }
      return result;
    }

    public Result<Activity> getActivityById(Long activityId) {
      Result<Activity> result = new Result<>();
      try {
        Optional<Activity> activityOptional = activityRepository.findById(activityId);

        if (activityOptional.isPresent()) {
          Activity activity = activityOptional.get();
          result.setSuccess(true);
          result.setData(activity);
        }
      } catch (Exception e) {
        result.setSuccess(false);
        result.setError("Error al crear la actividad: " + e.getMessage());
      }
      return result;
    }
}

