package com.brightloop.billing.service;

import com.brightloop.billing.entity.*;
import com.brightloop.billing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;
    private final SubscriptionRepository subRepo;
    private final PaymentRepository paymentRepo;

    public InvoiceService(InvoiceRepository invoiceRepo,
                          SubscriptionRepository subRepo,
                          PaymentRepository paymentRepo) {
        this.invoiceRepo = invoiceRepo;
        this.subRepo = subRepo;
        this.paymentRepo = paymentRepo;
    }

    public List<Invoice> list() { return invoiceRepo.findAll(); }

    public List<Invoice> listForCustomer(Long customerId) {
        return invoiceRepo.findByCustomerId(customerId);
    }

    public Invoice get(Long id) {
        return invoiceRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));
    }

    /**
     * Run billing: generate invoices for every ACTIVE subscription whose nextBillDate <= today.
     * Duplicate prevention via unique billing_key constraint.
     */
    @Transactional
    public int runBilling() {
        LocalDate today = LocalDate.now();
        List<Subscription> due = subRepo.findDueForBilling(today);
        int created = 0;
        for (Subscription s : due) {
            int guard = 0;
            while (s.getNextBillDate() != null
                    && !s.getNextBillDate().isAfter(today)
                    && !s.getNextBillDate().isAfter(s.getExpiryDate())
                    && guard++ < 36) {
                String key = s.getId() + "@" + s.getNextBillDate();
                if (!invoiceRepo.existsByBillingKey(key)) {
                    invoiceRepo.save(buildInvoice(s, key, s.getNextBillDate()));
                    created++;
                }
                s.setNextBillDate(advance(s.getNextBillDate(), s.getPlan().getBillingPeriod()));
                subRepo.save(s);
            }
        }
        return created;
    }

    @Transactional
    public Invoice updateStatus(Long id, String status) {
        Invoice inv = get(id);
        switch (status.toUpperCase()) {
            case "CONFIRMED"  -> inv.setStatus(Invoice.Status.CONFIRMED);
            case "CANCELLED"  -> inv.setStatus(Invoice.Status.CANCELLED);
            case "PAID"       -> inv.setStatus(Invoice.Status.PAID);
            default -> throw new IllegalArgumentException("Unknown status: " + status);
        }
        return invoiceRepo.save(inv);
    }

    /** Returns paid amount for an invoice */
    public BigDecimal paidAmount(Long invoiceId) {
        return paymentRepo.sumByInvoiceId(invoiceId);
    }

    /** Returns outstanding balance */
    public BigDecimal balance(Invoice inv) {
        BigDecimal paid = paymentRepo.sumByInvoiceId(inv.getId());
        return inv.getTotal().subtract(paid).max(BigDecimal.ZERO);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Invoice buildInvoice(Subscription s, String key, LocalDate billingDate) {
        Invoice inv = new Invoice();
        int seq = invoiceRepo.findMaxInvoiceSeq() + 1;
        inv.setInvoiceNo(String.format("INV-%05d", seq));
        inv.setBillingKey(key);
        inv.setSubscription(s);
        inv.setCustomer(s.getCustomer());
        inv.setInvoiceDate(billingDate);
        inv.setDueDate(billingDate.plusDays(s.getPaymentTermsDays()));
        inv.setStatus(Invoice.Status.DRAFT);

        // ── Calculate totals server-side – never trust frontend ──
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        for (SubscriptionLine sl : s.getLines()) {
            BigDecimal lineAmt = sl.getUnitPrice().multiply(BigDecimal.valueOf(sl.getQuantity()));
            BigDecimal lineTax = lineAmt.multiply(sl.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineAmt);
            taxAmount = taxAmount.add(lineTax);

            InvoiceLine il = new InvoiceLine();
            il.setInvoice(inv);
            il.setDescription(sl.getDescription());
            il.setQuantity(sl.getQuantity());
            il.setUnitPrice(sl.getUnitPrice());
            il.setTaxRate(sl.getTaxRate());
            il.setLineTotal(lineAmt);
            inv.getLines().add(il);
        }

        inv.setSubtotal(subtotal);
        inv.setDiscountAmount(BigDecimal.ZERO);
        inv.setTaxAmount(taxAmount);
        inv.setTotal(subtotal.add(taxAmount));
        return inv;
    }

    private LocalDate advance(LocalDate d, Plan.BillingPeriod period) {
        return switch (period) {
            case DAILY   -> d.plusDays(1);
            case WEEKLY  -> d.plusWeeks(1);
            case MONTHLY -> d.plusMonths(1);
            case YEARLY  -> d.plusYears(1);
        };
    }
}
