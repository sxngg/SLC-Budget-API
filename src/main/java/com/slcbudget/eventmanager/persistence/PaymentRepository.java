package com.slcbudget.eventmanager.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slcbudget.eventmanager.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>  {
  
}
