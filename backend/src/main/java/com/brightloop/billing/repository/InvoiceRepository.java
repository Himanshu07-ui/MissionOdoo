package com.brightloop.billing.repository;

import com.brightloop.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    boolean existsByBillingKey(String billingKey);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByStatus(Invoice.Status status);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'CONFIRMED' AND i.dueDate < :today")
    List<Invoice> findOverdue(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(i.invoiceNo, 5) AS int)), 0) FROM Invoice i")
    int findMaxInvoiceSeq();
}
