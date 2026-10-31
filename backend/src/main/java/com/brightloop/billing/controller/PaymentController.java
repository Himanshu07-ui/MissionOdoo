package com.brightloop.billing.controller;

import com.brightloop.billing.dto.PaymentRequest;
import com.brightloop.billing.entity.Payment;
import com.brightloop.billing.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return paymentService.list().stream().map(this::toMap).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> record(@RequestBody PaymentRequest req) {
        try {
            return ResponseEntity.ok(toMap(paymentService.record(req)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toMap(Payment p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("invoiceId", p.getInvoice().getId());
        m.put("invoiceNo", p.getInvoice().getInvoiceNo());
        m.put("customerName", p.getInvoice().getCustomer().getName());
        m.put("amount", p.getAmount());
        m.put("method", p.getMethod());
        m.put("paymentDate", p.getPaymentDate());
        m.put("note", p.getNote());
        return m;
    }
}
