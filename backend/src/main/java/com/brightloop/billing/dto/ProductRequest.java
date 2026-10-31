package com.brightloop.billing.dto;

import java.math.BigDecimal;
import java.util.List;

public class ProductRequest {
    private String name;
    private String type;
    private BigDecimal salePrice;
    private BigDecimal costPrice;
    private String notes;
    private Long taxId;
    private List<VariantDto> variants;

    public static class VariantDto {
        private Long id;
        private String name;
        private BigDecimal priceAddon;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public BigDecimal getPriceAddon() { return priceAddon; }
        public void setPriceAddon(BigDecimal priceAddon) { this.priceAddon = priceAddon; }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Long getTaxId() { return taxId; }
    public void setTaxId(Long taxId) { this.taxId = taxId; }
    public List<VariantDto> getVariants() { return variants; }
    public void setVariants(List<VariantDto> variants) { this.variants = variants; }
}
