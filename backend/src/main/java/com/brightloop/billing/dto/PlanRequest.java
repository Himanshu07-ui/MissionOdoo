package com.brightloop.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PlanRequest {
    private String name;
    private BigDecimal price;
    private String billingPeriod;
    private int minQty = 1;
    private LocalDate availableFrom;
    private LocalDate availableTo;
    private boolean canAutoClose;
    private boolean canClose = true;
    private boolean canPause = true;
    private boolean canRenew = true;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getBillingPeriod() { return billingPeriod; }
    public void setBillingPeriod(String billingPeriod) { this.billingPeriod = billingPeriod; }
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
}
