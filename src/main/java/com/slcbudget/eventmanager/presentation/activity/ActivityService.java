package com.slcbudget.eventmanager.presentation.activity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import com.slcbudget.eventmanager.domain.Activity;
import com.slcbudget.eventmanager.domain.ActivityParticipants;
import com.slcbudget.eventmanager.domain.Debt;
import com.slcbudget.eventmanager.domain.Event;
import com.slcbudget.eventmanager.domain.EventContact;
import com.slcbudget.eventmanager.domain.Payment;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.domain.dto.ActivityCreateDTO;
import com.slcbudget.eventmanager.persistence.ActivityParticipantsRepository;
import com.slcbudget.eventmanager.persistence.ActivityRepository;
import com.slcbudget.eventmanager.persistence.DebtRepository;
import com.slcbudget.eventmanager.persistence.EventContactRepository;
import com.slcbudget.eventmanager.persistence.EventRepository;
import com.slcbudget.eventmanager.persistence.PaymentRepository;
import com.slcbudget.eventmanager.persistence.UserRepository;
import com.slcbudget.eventmanager.presentation.event_contact.EventContactService;
import com.slcbudget.eventmanager.utils.Result;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.Set;

@Service
public class ActivityService {

  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private ActivityParticipantsRepository activityParticipantsRepository;

  @Autowired
  private EventContactRepository eventContactRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private BalanceService balanceService;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private DebtRepository debtRepository;

  @Autowired
  private EventContactService eventContactService;

  @Transactional
  public Result<Activity> createActivity(ActivityCreateDTO activityDTO) {
    Result<Activity> result = new Result<>();
    try {
      Event event = eventRepository.findById(activityDTO.eventId())
          .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

      Activity activity = new Activity();
      activity.setDescription(activityDTO.description());
      activity.setValue(activityDTO.value());
      activity.setIsPaid(false);
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

        totalStaticValues = totalStaticValues.add(participantData.staticValue());
        totalPercent = totalPercent.add(participantData.participationPercentage());

        // ActivityParticipants
        ActivityParticipants activityParticipant = new ActivityParticipants();
        balanceService.updateActivityParticipantsBalances(activityParticipant, activity,
            participantData.staticValue(), participantData.participationPercentage(), participant);

        // User
        balanceService.updateUserBalances(participant, participantData.staticValue());

        // EventContact
        balanceService.updateEventContactBalance(event.getEvent_id(), participant.getId(),
            participantData.staticValue());
      }

      if (totalStaticValues.compareTo(activity.getValue()) > 0
          || totalPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
        result.setSuccess(false);
        result.setError("La sumatoria de los valores o porcentajes excede el valor total de la actividad. ");
        throw new RuntimeException("La sumatoria de los porcentajes excede el 100%.");
      }
      balanceService.updateEventBalance(activity);

      activity.setParticipants(activityParticipantsRepository.findByActivity(activity));

      result.setSuccess(true);
      result.setData(activity);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setError("Error al crear la actividad: " + e.getMessage());
    }
    return result;
  }

  
  public void payDebts(Activity activity, UserEntity payer, BigDecimal totalAmount, Long eventId) {
    Set<ActivityParticipants> participants = activity.getParticipants();
    // Calcular el porcentaje del total que cada participante debe asumir
    BigDecimal totalParticipants = BigDecimal.valueOf(participants.size());
    BigDecimal perParticipantAmount = totalAmount.divide(totalParticipants, RoundingMode.HALF_UP);

    // Iterar sobre los participantes y saldar sus deudas
    for (ActivityParticipants participant : participants) {
      UserEntity participantUser = participant.getParticipant();
      if (!participantUser.equals(payer)) {
        // Calcular el nuevo saldo para el participante
        BigDecimal participantBalance = participantUser.getBalance().subtract(perParticipantAmount);
        // Actualizar el saldo del participante
        participantUser.setBalance(participantBalance);

        // Guardar en la base de datos
        userRepository.save(participantUser);
        EventContact eventContact = eventContactService.getEventContactByContactId(participantUser.getId(), eventId);
        BigDecimal eventContactBalance = eventContact.getBalance().subtract(perParticipantAmount);
        eventContact.setBalance(eventContactBalance);
        eventContactRepository.save(eventContact);
        BigDecimal activityParticipantsBalance = participant.getBalance().subtract(perParticipantAmount);
        participant.setBalance(activityParticipantsBalance);
        activityParticipantsRepository.save(participant);

        // Generar deuda
        Debt existingDebt = debtRepository.findByDebtorAndCreditor(participantUser, payer);
        Debt existingDebtInverse = debtRepository.findByDebtorAndCreditor(payer, participantUser);

        if (existingDebtInverse != null && existingDebtInverse.getAmount().compareTo(BigDecimal.ZERO) > 0) {
          //Si el que paga tiene una deuda con el participante entonces el pagador le tiene que pagar
          BigDecimal existingDebtAmount = existingDebtInverse.getAmount().subtract(perParticipantAmount);
          existingDebtInverse.setAmount(existingDebtAmount);
          
          debtRepository.save(existingDebtInverse);
        } else if (existingDebt != null) {
          // Si el participante tiene una deuda con el pagador entonces se aumenta la deuda
          BigDecimal existingDebtAmount = existingDebt.getAmount().add(perParticipantAmount);
          existingDebt.setAmount(existingDebtAmount);

          debtRepository.save(existingDebt);
        } else {
          // Si no hay deuda existente, crear una nueva deuda
          Debt newDebt = new Debt();
          newDebt.setDebtor(participantUser);
          newDebt.setCreditor(payer);
          newDebt.setAmount(perParticipantAmount);
          newDebt.setPaid(false);
          debtRepository.save(newDebt);
        }
      }
    }

    activity.setIsPaid(true);
    activityRepository.save(activity);

    BigDecimal payerBalance = payer.getBalance().subtract(perParticipantAmount);
    payer.setBalance(payerBalance);
    userRepository.save(payer);

    EventContact payerEventContact = eventContactService.getEventContactByContactId(payer.getId(), eventId);
    BigDecimal payerEventContactBalance = payerEventContact.getBalance().subtract(perParticipantAmount);
    payerEventContact.setBalance(payerEventContactBalance);
    eventContactRepository.save(payerEventContact);
  }

  /**
   * public Result<Activity> editActivity(Long activityId, ActivityCreateDTO
   * activityDTO) {
   * Result<Activity> result = new Result<>();
   * try {
   * // Obtén la actividad existente
   * Activity existingActivity = activityRepository.findById(activityId)
   * .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con
   * id: " + activityId));
   * 
   * // Realiza las actualizaciones necesarias en la actividad existente
   * existingActivity.setDescription(activityDTO.description());
   * existingActivity.setValue(activityDTO.value());
   * 
   * // Borra los participantes actuales (si es necesario) y guarda los nuevos
   * deleteParticipants(existingActivity);
   * saveParticipants(existingActivity, activityDTO.participationData());
   * 
   * // Realiza las validaciones necesarias, por ejemplo, la suma de porcentajes o
   * // valores
   * validateActivity(existingActivity);
   * 
   * // Guarda los cambios
   * activityRepository.save(existingActivity);
   * 
   * // Actualiza los participantes y devuelve el resultado
   * existingActivity.setParticipants(activityParticipantsRepository.findByActivity(existingActivity));
   * 
   * result.setSuccess(true);
   * result.setData(existingActivity);
   * } catch (Exception e) {
   * result.setSuccess(false);
   * result.setError("Error al editar la actividad: " + e.getMessage());
   * }
   * return result;
   * }
   * 
   * private void saveParticipants(Activity activity, Map<Long,
   * ActivityCreateDTO.ParticipationData> participationData) {
   * for (Map.Entry<Long, ActivityCreateDTO.ParticipationData> entry :
   * participationData.entrySet()) {
   * Long participantId = entry.getKey();
   * ActivityCreateDTO.ParticipationData participantData = entry.getValue();
   * 
   * UserEntity participant = userRepository.findById(participantId)
   * .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id:
   * " + participantId));
   * 
   * ActivityParticipants activityParticipant = new ActivityParticipants();
   * activityParticipant.setActivity(activity);
   * activityParticipant.setParticipant(participant);
   * activityParticipant.setParticipationPercent(participantData.participationPercentage());
   * activityParticipant.setStaticValueParticipation(participantData.staticValue());
   * 
   * activityParticipantsRepository.save(activityParticipant);
   * }
   * }
   * 
   * private void validateActivity(Activity activity) {
   * BigDecimal totalStaticValues = BigDecimal.ZERO;
   * BigDecimal totalPercent = BigDecimal.ZERO;
   * 
   * for (ActivityParticipants participant :
   * activityParticipantsRepository.findByActivity(activity)) {
   * totalStaticValues =
   * totalStaticValues.add(participant.getStaticValueParticipation());
   * totalPercent = totalPercent.add(participant.getParticipationPercent());
   * }
   * 
   * if (totalStaticValues.compareTo(activity.getValue()) > 0
   * || totalPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
   * throw new RuntimeException("La sumatoria de los porcentajes o valores excede
   * el límite.");
   * }
   * }
   * 
   * private void deleteParticipants(Activity activity) {
   * Set<ActivityParticipants> participantsToDelete = activity.getParticipants();
   * for (ActivityParticipants participant : participantsToDelete) {
   * activityParticipantsRepository.delete(participant);
   * }
   * }
   * 
   */
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
