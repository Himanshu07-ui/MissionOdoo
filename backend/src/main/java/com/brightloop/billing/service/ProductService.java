package com.brightloop.billing.service;

import com.brightloop.billing.dto.ProductRequest;
import com.brightloop.billing.entity.Product;
import com.brightloop.billing.entity.ProductVariant;
import com.brightloop.billing.entity.Tax;
import com.brightloop.billing.repository.ProductRepository;
import com.brightloop.billing.repository.TaxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final TaxRepository taxRepo;

    public ProductService(ProductRepository productRepo, TaxRepository taxRepo) {
        this.productRepo = productRepo;
        this.taxRepo = taxRepo;
    }

    public List<Product> list() {
        return productRepo.findAll();
    }

    public Product get(Long id) {
        return productRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Transactional
    public Product create(ProductRequest req) {
        Product p = new Product();
        return save(p, req);
    }

    @Transactional
    public Product update(Long id, ProductRequest req) {
        Product p = get(id);
        return save(p, req);
    }

    @Transactional
    public void delete(Long id) {
        productRepo.deleteById(id);
    }

    private Product save(Product p, ProductRequest req) {
        p.setName(req.getName());
        p.setType(req.getType());
        p.setSalePrice(req.getSalePrice());
        p.setCostPrice(req.getCostPrice() != null ? req.getCostPrice() : java.math.BigDecimal.ZERO);
        p.setNotes(req.getNotes());
        if (req.getTaxId() != null) {
            Tax tax = taxRepo.findById(req.getTaxId()).orElse(null);
            p.setTax(tax);
        }
        // Update variants
        p.getVariants().clear();
        if (req.getVariants() != null) {
            for (ProductRequest.VariantDto vd : req.getVariants()) {
                ProductVariant v = new ProductVariant();
                v.setProduct(p);
                v.setName(vd.getName());
                v.setPriceAddon(vd.getPriceAddon() != null ? vd.getPriceAddon() : java.math.BigDecimal.ZERO);
                p.getVariants().add(v);
            }
        }
        return productRepo.save(p);
    }
}
