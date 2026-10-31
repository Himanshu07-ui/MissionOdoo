package com.brightloop.billing.dto;

import java.math.BigDecimal;

public class DashboardResponse {
    private long activeSubscriptions;
    private BigDecimal monthlyRecurringRevenue;
    private BigDecimal revenueCollected;
    private BigDecimal outstandingAmount;
    private long overdueInvoiceCount;

    public DashboardResponse(long activeSubscriptions, BigDecimal monthlyRecurringRevenue,
                             BigDecimal revenueCollected, BigDecimal outstandingAmount, long overdueInvoiceCount) {
        this.activeSubscriptions = activeSubscriptions;
        this.monthlyRecurringRevenue = monthlyRecurringRevenue;
        this.revenueCollected = revenueCollected;
        this.outstandingAmount = outstandingAmount;
        this.overdueInvoiceCount = overdueInvoiceCount;
    }

    public long getActiveSubscriptions() { return activeSubscriptions; }
    public BigDecimal getMonthlyRecurringRevenue() { return monthlyRecurringRevenue; }
    public BigDecimal getRevenueCollected() { return revenueCollected; }
    public BigDecimal getOutstandingAmount() { return outstandingAmount; }
    public long getOverdueInvoiceCount() { return overdueInvoiceCount; }
}
