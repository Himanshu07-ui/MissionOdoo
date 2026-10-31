package com.brightloop.billing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_period", nullable = false, length = 20)
    private BillingPeriod billingPeriod = BillingPeriod.MONTHLY;

    @Column(name = "min_qty", nullable = false)
    private int minQty = 1;

    @Column(name = "available_from", nullable = false)
    private LocalDate availableFrom;

    @Column(name = "available_to", nullable = false)
    private LocalDate availableTo;

    @Column(name = "can_auto_close", nullable = false)
    private boolean canAutoClose = false;

    @Column(name = "can_close", nullable = false)
    private boolean canClose = true;

    @Column(name = "can_pause", nullable = false)
    private boolean canPause = true;

    @Column(name = "can_renew", nullable = false)
    private boolean canRenew = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum BillingPeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BillingPeriod getBillingPeriod() { return billingPeriod; }
    public void setBillingPeriod(BillingPeriod billingPeriod) { this.billingPeriod = billingPeriod; }
    public int getMinQty() { return minQty; }
    public void setMinQty(int minQty) { this.minQty = minQty; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }
    public LocalDate getAvailableTo() { return availableTo; }
    public void setAvailableTo(LocalDate availableTo) { this.availableTo = availableTo; }
    public boolean isCanAutoClose() { return canAutoClose; }
    public void setCanAutoClose(boolean canAutoClose) { this.canAutoClose = canAutoClose; }
    public boolean isCanClose() { return canClose; }
    public void setCanClose(boolean canClose) { this.canClose = canClose; }
    public boolean isCanPause() { return canPause; }
    public void setCanPause(boolean canPause) { this.canPause = canPause; }
    public boolean isCanRenew() { return canRenew; }
    public void setCanRenew(boolean canRenew) { this.canRenew = canRenew; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
