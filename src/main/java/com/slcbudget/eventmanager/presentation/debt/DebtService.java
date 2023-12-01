package com.slcbudget.eventmanager.presentation.debt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.slcbudget.eventmanager.domain.Debt;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.domain.projections.ActivityParticipantsProjection;
import com.slcbudget.eventmanager.domain.projections.DebtProjection;
import com.slcbudget.eventmanager.persistence.DebtRepository;
import com.slcbudget.eventmanager.persistence.UserRepository;
import com.slcbudget.eventmanager.utils.Result;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;

import java.math.BigDecimal;

@Service
public class DebtService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private DebtRepository debtRepository;

  public Result<Page<DebtProjection>> getDebtsCreditorId(Long userId,
      Pageable pagination) {
    Result<Page<DebtProjection>> result = new Result<>();

    try {
      Page<DebtProjection> actPage = debtRepository.findByCreditorAndPaidIsFalse(userId, false,
          pagination);
      result.setSuccess(true);
      result.setData(actPage);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setError("Error para recuperar las deudas del usuario" + e.getMessage());
    }
    return result;
  }

  public Result<Page<DebtProjection>> getDebtsDebtorByUserId(Long userId,
      Pageable pagination) {
    Result<Page<DebtProjection>> result = new Result<>();

    try {
      Page<DebtProjection> actPage = debtRepository.findByDebitorIdAndPaidIsFalse(userId, false,
          pagination);
      result.setSuccess(true);
      result.setData(actPage);
    } catch (Exception e) {
      result.setSuccess(false);
      result.setError("Error para recuperar las deudas del usuario" + e.getMessage());
    }
    return result;
  }

  @Transactional
  public Debt payDebt(Long creditorId, Long debtorId, BigDecimal amount) {
    UserEntity debtor = userRepository.findById(debtorId)
        .orElseThrow(() -> new EntityNotFoundException("Debtor not found with id: " + debtorId));

    UserEntity creditor = userRepository.findById(creditorId)
        .orElseThrow(() -> new EntityNotFoundException("Creditor not found with id: " + creditorId));

    Debt debt = debtRepository.findByDebtorAndCreditor(debtor, creditor);
    if (debt == null) {
      throw new EntityNotFoundException("Deuda no encontrada entre deudor y acreedor");
    }

    if (debt.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalStateException("Debt is already paid");
    } else {
      BigDecimal newAmount = debt.getAmount().subtract(amount);
      debt.setAmount(newAmount);
    }

    return debtRepository.save(debt);
  }
}
