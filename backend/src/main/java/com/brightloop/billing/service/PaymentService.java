package com.brightloop.billing.service;

import com.brightloop.billing.dto.PaymentRequest;
import com.brightloop.billing.entity.Invoice;
import com.brightloop.billing.entity.Payment;
import com.brightloop.billing.repository.InvoiceRepository;
import com.brightloop.billing.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final InvoiceRepository invoiceRepo;
    private final InvoiceService invoiceService;

    public PaymentService(PaymentRepository paymentRepo, InvoiceRepository invoiceRepo, InvoiceService invoiceService) {
        this.paymentRepo = paymentRepo;
        this.invoiceRepo = invoiceRepo;
        this.invoiceService = invoiceService;
    }

    public List<Payment> list() {
        return paymentRepo.findAllByOrderByPaymentDateDesc();
    }

    @Transactional
    public Payment record(PaymentRequest req) {
        Invoice inv = invoiceService.get(req.getInvoiceId());
        if (inv.getStatus() == Invoice.Status.CANCELLED) {
            throw new IllegalStateException("Cannot record payment against a cancelled invoice.");
        }

        BigDecimal outstanding = invoiceService.balance(inv);
        if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (req.getAmount().compareTo(outstanding.add(new BigDecimal("0.01"))) > 0) {
            throw new IllegalArgumentException("Amount exceeds outstanding balance of " + outstanding);
        }

        Payment p = new Payment();
        p.setInvoice(inv);
        p.setAmount(req.getAmount());
        p.setMethod(Payment.Method.valueOf(req.getMethod().toUpperCase().replace(" ", "_")));
        p.setPaymentDate(req.getPaymentDate() != null ? req.getPaymentDate() : LocalDate.now());
        p.setNote(req.getNote());
        paymentRepo.save(p);

        // Auto-confirm draft invoice on first payment
        if (inv.getStatus() == Invoice.Status.DRAFT) {
            inv.setStatus(Invoice.Status.CONFIRMED);
        }
        // Mark paid if fully settled
        BigDecimal newBalance = invoiceService.balance(inv);
        if (newBalance.compareTo(new BigDecimal("0.01")) < 0) {
            inv.setStatus(Invoice.Status.PAID);
        }
        invoiceRepo.save(inv);
        return p;
    }
}
