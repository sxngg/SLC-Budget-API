package com.slcbudget.eventmanager.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.slcbudget.eventmanager.domain.Debt;
import com.slcbudget.eventmanager.domain.UserEntity;
import com.slcbudget.eventmanager.domain.projections.DebtProjection;


public interface DebtRepository extends JpaRepository<Debt, Long> {

  @Query("SELECT " +
    "d.debtor.id AS userDebtorId, " +
    "d.debtor.name AS userDebtorName, " +
    "d.debtor.email AS userDebtorEmail, " +
    "d.debtor.profileImage AS userDebtorPicture, " +
    "d.creditor.id AS userCreditorId, " +
    "d.creditor.name AS userCreditorName, " +
    "d.creditor.email AS userCreditorEmail, " +
    "d.creditor.profileImage AS userCreditorPicture, " +
    "d.amount AS amount, " + 
    "d.paid AS isPaid " +
    "FROM Debt d WHERE d.creditor.id = :creditorId AND d.paid = :isPaid")
  Page<DebtProjection> findByCreditorAndPaidIsFalse(@Param("creditorId") Long creditor,
      @Param("isPaid") boolean isPaid, Pageable pagination);

  @Query("SELECT " +
    "d.debtor.id AS userDebtorId, " +
    "d.debtor.name AS userDebtorName, " +
    "d.debtor.email AS userDebtorEmail, " +
    "d.debtor.profileImage AS userDebtorPicture, " +
    "d.creditor.id AS userCreditorId, " +
    "d.creditor.name AS userCreditorName, " +
    "d.creditor.email AS userCreditorEmail, " +
    "d.creditor.profileImage AS userCreditorPicture, " +
    "d.amount AS amount, " + 
    "d.paid AS isPaid " +
    "FROM Debt d WHERE d.debtor.id = :debtorId AND d.paid = :isPaid")
  Page<DebtProjection> findByDebitorIdAndPaidIsFalse(@Param("debtorId") Long debtor,
    @Param("isPaid") boolean isPaid, Pageable pagination);

  Debt findByDebtorAndCreditor(UserEntity debtor, UserEntity creditor);
}
