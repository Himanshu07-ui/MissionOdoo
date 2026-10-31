package com.brightloop.billing.service;

import com.brightloop.billing.dto.SubscriptionRequest;
import com.brightloop.billing.entity.*;
import com.brightloop.billing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subRepo;
    private final UserRepository userRepo;
    private final PlanRepository planRepo;
    private final ProductRepository productRepo;

    // simple in-memory counter backed by DB sequences table on startup
    private final AtomicInteger subSeq = new AtomicInteger(1);

    public SubscriptionService(SubscriptionRepository subRepo, UserRepository userRepo,
                               PlanRepository planRepo, ProductRepository productRepo) {
        this.subRepo = subRepo;
        this.userRepo = userRepo;
        this.planRepo = planRepo;
        this.productRepo = productRepo;
    }

    public List<Subscription> list() { return subRepo.findAll(); }

    public List<Subscription> listForCustomer(Long customerId) {
        return subRepo.findByCustomerId(customerId);
    }

    public Subscription get(Long id) {
        return subRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + id));
    }

    @Transactional
    public Subscription create(SubscriptionRequest req) {
        User customer = userRepo.findById(req.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        Plan plan = planRepo.findById(req.getPlanId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        Subscription s = new Subscription();
        // Generate a unique number
        int seq = subRepo.findAll().stream()
                .mapToInt(x -> {
                    try { return Integer.parseInt(x.getSubscriptionNo().replace("SUB-", "")); }
                    catch (NumberFormatException e) { return 0; }
                }).max().orElse(0) + 1;
        s.setSubscriptionNo(String.format("SUB-%04d", seq));
        s.setCustomer(customer);
        s.setPlan(plan);
        s.setStartDate(req.getStartDate());
        s.setExpiryDate(req.getExpiryDate());
        s.setNextBillDate(req.getStartDate());
        s.setPaymentTermsDays(req.getPaymentTermsDays());
        s.setStatus(Subscription.Status.DRAFT);

        // Build lines
        if (req.getLines() != null) {
            for (SubscriptionRequest.LineDto ld : req.getLines()) {
                SubscriptionLine line = new SubscriptionLine();
                line.setSubscription(s);
                line.setDescription(ld.getDescription());
                line.setQuantity(ld.getQuantity());
                line.setUnitPrice(ld.getUnitPrice());
                line.setTaxRate(ld.getTaxRate() != null ? ld.getTaxRate() : java.math.BigDecimal.ZERO);
                if (ld.getProductId() != null) {
                    productRepo.findById(ld.getProductId()).ifPresent(line::setProduct);
                }
                s.getLines().add(line);
            }
        }
        return subRepo.save(s);
    }

    @Transactional
    public Subscription performAction(Long id, String action) {
        Subscription s = get(id);
        Plan plan = s.getPlan();
        switch (action.toUpperCase()) {
            case "CONFIRM"  -> s.setStatus(Subscription.Status.CONFIRMED);
            case "ACTIVATE" -> { s.setStatus(Subscription.Status.ACTIVE);
                                  if (s.getNextBillDate() == null) s.setNextBillDate(s.getStartDate()); }
            case "PAUSE"    -> { if (!plan.isCanPause()) throw new IllegalStateException("Plan does not allow pausing");
                                  s.setStatus(Subscription.Status.PAUSED); }
            case "RESUME"   -> { s.setStatus(Subscription.Status.ACTIVE);
                                  if (s.getNextBillDate() == null) s.setNextBillDate(LocalDate.now()); }
            case "CLOSE"    -> { if (!plan.isCanClose()) throw new IllegalStateException("Plan does not allow early closure");
                                  s.setStatus(Subscription.Status.CLOSED); }
            case "RENEW"    -> { if (!plan.isCanRenew()) throw new IllegalStateException("Plan does not allow renewal");
                                  s.setExpiryDate(advanceByOne(s.getExpiryDate(), plan.getBillingPeriod()));
                                  s.setStatus(Subscription.Status.ACTIVE); }
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        }
        return subRepo.save(s);
    }

    private LocalDate advanceByOne(LocalDate d, Plan.BillingPeriod period) {
        return switch (period) {
            case DAILY   -> d.plusDays(1);
            case WEEKLY  -> d.plusWeeks(1);
            case MONTHLY -> d.plusMonths(1);
            case YEARLY  -> d.plusYears(1);
        };
    }
}
