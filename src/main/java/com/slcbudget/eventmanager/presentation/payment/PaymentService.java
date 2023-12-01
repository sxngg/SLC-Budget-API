package com.slcbudget.eventmanager.presentation.payment;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.slcbudget.eventmanager.domain.Activity;
import com.slcbudget.eventmanager.domain.EventContact;
import com.slcbudget.eventmanager.domain.Payment;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.persistence.ActivityRepository;
import com.slcbudget.eventmanager.persistence.PaymentRepository;
import com.slcbudget.eventmanager.persistence.UserRepository;
import com.slcbudget.eventmanager.presentation.activity.ActivityService;
import com.slcbudget.eventmanager.presentation.event_contact.EventContactService;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PaymentService {

  @Autowired
  private ActivityService activityService;

  @Autowired
  private ActivityRepository activityRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired  
  private EventContactService eventContactService;
  
  public void registerPayment(Long activityId, Long payerId, BigDecimal amount, Long eventId) {
    // Obtener actividad y usuarios
    Activity activity = activityRepository.findById(activityId)
      .orElseThrow(() -> new EntityNotFoundException("Activity not found"));
    UserEntity payer = userRepository.findById(payerId)
      .orElseThrow(() -> new EntityNotFoundException("Payer not found"));

    // Actualizar saldos
    activityService.payDebts(activity, payer, amount, eventId);
    payer.setBalance(payer.getBalance().subtract(amount));
    
    // Guardar en la base de datos
    userRepository.save(payer);
  }
}
