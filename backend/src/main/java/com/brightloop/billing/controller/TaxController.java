package com.brightloop.billing.controller;

import com.brightloop.billing.entity.Tax;
import com.brightloop.billing.repository.TaxRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taxes")
public class TaxController {

    private final TaxRepository taxRepo;

    public TaxController(TaxRepository taxRepo) {
        this.taxRepo = taxRepo;
    }

    @GetMapping
    public List<Tax> list() { return taxRepo.findAll(); }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Tax t = new Tax();
            t.setName((String) body.get("name"));
            t.setRate(new java.math.BigDecimal(body.get("rate").toString()));
            return ResponseEntity.ok(taxRepo.save(t));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        taxRepo.deleteById(id);
        return ResponseEntity.ok(Map.of("deleted", id));
    }
}
