package com.brightloop.billing.controller;

import com.brightloop.billing.config.JwtFilter;
import com.brightloop.billing.entity.Invoice;
import com.brightloop.billing.entity.User;
import com.brightloop.billing.repository.PaymentRepository;
import com.brightloop.billing.service.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final PaymentRepository paymentRepo;

    public InvoiceController(InvoiceService invoiceService, PaymentRepository paymentRepo) {
        this.invoiceService = invoiceService;
        this.paymentRepo = paymentRepo;
    }

    @GetMapping
    public List<Map<String, Object>> list(HttpServletRequest req) {
        User user = (User) req.getAttribute(JwtFilter.ATTR);
        List<Invoice> invoices = (user != null && user.getRole() == User.Role.CUSTOMER)
                ? invoiceService.listForCustomer(user.getId())
                : invoiceService.list();
        return invoices.stream().map(this::toMap).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long id) {
        return ResponseEntity.ok(toMap(invoiceService.get(id)));
    }

    @PostMapping("/billing/run")
    public ResponseEntity<Map<String, Object>> runBilling() {
        int created = invoiceService.runBilling();
        return ResponseEntity.ok(Map.of("invoicesCreated", created));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Invoice inv = invoiceService.updateStatus(id, body.get("status"));
            return ResponseEntity.ok(toMap(inv));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> toMap(Invoice i) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", i.getId());
        m.put("invoiceNo", i.getInvoiceNo());
        m.put("billingKey", i.getBillingKey());
        m.put("subscriptionId", i.getSubscription().getId());
        m.put("customerId", i.getCustomer().getId());
        m.put("customerName", i.getCustomer().getName());
        m.put("customerEmail", i.getCustomer().getEmail());
        m.put("invoiceDate", i.getInvoiceDate());
        m.put("dueDate", i.getDueDate());
        m.put("subtotal", i.getSubtotal());
        m.put("discountAmount", i.getDiscountAmount());
        m.put("taxAmount", i.getTaxAmount());
        m.put("total", i.getTotal());
        m.put("paidAmount", invoiceService.paidAmount(i.getId()));
        m.put("balance", invoiceService.balance(i));
        m.put("status", i.getStatus());
        m.put("sent", i.isSent());
        m.put("lines", i.getLines().stream().map(l -> {
            Map<String, Object> lm = new LinkedHashMap<>();
            lm.put("description", l.getDescription());
            lm.put("quantity", l.getQuantity());
            lm.put("unitPrice", l.getUnitPrice());
            lm.put("taxRate", l.getTaxRate());
            lm.put("lineTotal", l.getLineTotal());
            return lm;
        }).collect(Collectors.toList()));
        return m;
    }
}
