package com.brightloop.billing.controller;

import com.brightloop.billing.dto.PlanRequest;
import com.brightloop.billing.entity.Plan;
import com.brightloop.billing.service.PlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public List<Plan> list() { return planService.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> get(@PathVariable Long id) {
        return ResponseEntity.ok(planService.get(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PlanRequest req) {
        try { return ResponseEntity.ok(planService.create(req)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PlanRequest req) {
        try { return ResponseEntity.ok(planService.update(id, req)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        planService.delete(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }
}
