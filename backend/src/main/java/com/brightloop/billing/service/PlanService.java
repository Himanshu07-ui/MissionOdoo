package com.brightloop.billing.service;

import com.brightloop.billing.dto.PlanRequest;
import com.brightloop.billing.entity.Plan;
import com.brightloop.billing.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepo;

    public PlanService(PlanRepository planRepo) {
        this.planRepo = planRepo;
    }

    public List<Plan> list() { return planRepo.findAll(); }

    public Plan get(Long id) {
        return planRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Plan not found: " + id));
    }

    @Transactional
    public Plan create(PlanRequest req) {
        return planRepo.save(toEntity(new Plan(), req));
    }

    @Transactional
    public Plan update(Long id, PlanRequest req) {
        return planRepo.save(toEntity(get(id), req));
    }

    @Transactional
    public void delete(Long id) { planRepo.deleteById(id); }

    private Plan toEntity(Plan p, PlanRequest req) {
        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setBillingPeriod(Plan.BillingPeriod.valueOf(req.getBillingPeriod().toUpperCase()));
        p.setMinQty(Math.max(1, req.getMinQty()));
        p.setAvailableFrom(req.getAvailableFrom());
        p.setAvailableTo(req.getAvailableTo());
        p.setCanAutoClose(req.isCanAutoClose());
        p.setCanClose(req.isCanClose());
        p.setCanPause(req.isCanPause());
        p.setCanRenew(req.isCanRenew());
        return p;
    }
}
