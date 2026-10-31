package com.brightloop.billing.repository;

import com.brightloop.billing.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByCustomerId(Long customerId);

    List<Subscription> findByStatus(Subscription.Status status);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.nextBillDate <= :today AND s.expiryDate >= :today")
    List<Subscription> findDueForBilling(LocalDate today);
}
