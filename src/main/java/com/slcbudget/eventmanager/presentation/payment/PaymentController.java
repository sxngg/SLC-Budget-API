package com.slcbudget.eventmanager.presentation.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.slcbudget.eventmanager.domain.dto.PaymentRequestDTO;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/register")
    public ResponseEntity<String> makePayment(@RequestBody PaymentRequestDTO paymentRequest) {
        paymentService.registerPayment(paymentRequest.activityId(), paymentRequest.payerId(),
          paymentRequest.amount(), paymentRequest.eventId());
        return ResponseEntity.ok("Pago registrado con éxito");
    }
}
