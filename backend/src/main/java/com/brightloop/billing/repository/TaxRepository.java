package com.brightloop.billing.repository;

import com.brightloop.billing.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxRepository extends JpaRepository<Tax, Long> {}
