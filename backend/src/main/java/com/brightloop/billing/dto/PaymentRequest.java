package com.brightloop.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentRequest {
    private Long invoiceId;
    private BigDecimal amount;
    private String method;
    private LocalDate paymentDate;
    private String note;

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
