package com.brightloop.billing.service;

import com.brightloop.billing.dto.DashboardResponse;
import com.brightloop.billing.entity.Invoice;
import com.brightloop.billing.entity.Plan;
import com.brightloop.billing.entity.Subscription;
import com.brightloop.billing.entity.SubscriptionLine;
import com.brightloop.billing.repository.InvoiceRepository;
import com.brightloop.billing.repository.PaymentRepository;
import com.brightloop.billing.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final SubscriptionRepository subRepo;
    private final InvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;
    private final InvoiceService invoiceService;

    public DashboardService(SubscriptionRepository subRepo, InvoiceRepository invoiceRepo,
                            PaymentRepository paymentRepo, InvoiceService invoiceService) {
        this.subRepo = subRepo;
        this.invoiceRepo = invoiceRepo;
        this.paymentRepo = paymentRepo;
        this.invoiceService = invoiceService;
    }

    public DashboardResponse compute(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate f = from != null ? from : LocalDate.of(1900, 1, 1);
        LocalDate t = to   != null ? to   : LocalDate.of(9999, 12, 31);

        // Active subscriptions
        List<Subscription> active = subRepo.findByStatus(Subscription.Status.ACTIVE);

        // Monthly Recurring Revenue
        BigDecimal mrr = active.stream()
                .map(s -> periodToMonthly(subTotal(s), s.getPlan().getBillingPeriod()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Revenue collected (filtered by date range)
        BigDecimal collected = paymentRepo.sumByDateRange(f, t);

        // Outstanding (confirmed invoices)
        List<Invoice> confirmed = invoiceRepo.findByStatus(Invoice.Status.CONFIRMED);
        BigDecimal outstanding = confirmed.stream()
                .map(i -> invoiceService.balance(i))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Overdue
        long overdue = invoiceRepo.findOverdue(today).size();

        return new DashboardResponse(active.size(), mrr, collected, outstanding, overdue);
    }

    private BigDecimal subTotal(Subscription s) {
        return s.getLines().stream()
                .map(l -> l.getUnitPrice().multiply(BigDecimal.valueOf(l.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal periodToMonthly(BigDecimal amount, Plan.BillingPeriod period) {
        return switch (period) {
            case DAILY   -> amount.multiply(new BigDecimal("30"));
            case WEEKLY  -> amount.multiply(new BigDecimal("4.33"));
            case MONTHLY -> amount;
            case YEARLY  -> amount.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        };
    }
}
