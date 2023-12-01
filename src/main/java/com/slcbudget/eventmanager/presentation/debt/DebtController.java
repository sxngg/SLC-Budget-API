package com.slcbudget.eventmanager.presentation.debt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slcbudget.eventmanager.domain.dto.PayRequestDTO;
import com.slcbudget.eventmanager.domain.projections.DebtProjection;
import com.slcbudget.eventmanager.utils.Result;

@RestController
@RequestMapping("/debts")
public class DebtController {

  @Autowired
  private DebtService debtService;

  @GetMapping("/creditor/{userId}")
  public ResponseEntity<?> getCreditorByUserId(@PathVariable Long userId,
    @PageableDefault(size = 3) Pageable pagination) {
    Result<Page<DebtProjection>> result = debtService.getDebtsCreditorId(userId, pagination);
  
    if (result.isSuccess()) {
      return ResponseEntity.ok(result.getData());
  } else {
      return ResponseEntity.badRequest().body(result.getError());
  }
  }

    @GetMapping("/debtor/{userId}")
  public ResponseEntity<?> getDebtorByUserId(@PathVariable Long userId,
    @PageableDefault(size = 3) Pageable pagination) {
    Result<Page<DebtProjection>> result = debtService.getDebtsDebtorByUserId(userId, pagination);
    if (result.isSuccess()) {
      return ResponseEntity.ok(result.getData());
  } else {
      return ResponseEntity.badRequest().body(result.getError());
  }
  }


  @PostMapping("/pay")
  public ResponseEntity<?> payDebts(@RequestBody PayRequestDTO payRequestDTO) {
    try {
      debtService.payDebt(payRequestDTO.creditorId(), payRequestDTO.debtorId(), payRequestDTO.amount());
      return ResponseEntity.ok("Pagado");
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}
