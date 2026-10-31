package com.brightloop.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SubscriptionRequest {
    private Long customerId;
    private Long planId;
    private LocalDate startDate;
    private LocalDate expiryDate;
    private int paymentTermsDays = 15;
    private List<LineDto> lines;

    public static class LineDto {
        private Long productId;
        private Long variantId;
        private String description;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal taxRate;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getTaxRate() { return taxRate; }
        public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public int getPaymentTermsDays() { return paymentTermsDays; }
    public void setPaymentTermsDays(int paymentTermsDays) { this.paymentTermsDays = paymentTermsDays; }
    public List<LineDto> getLines() { return lines; }
    public void setLines(List<LineDto> lines) { this.lines = lines; }
}
