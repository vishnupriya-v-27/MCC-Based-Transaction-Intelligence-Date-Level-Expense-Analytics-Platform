package com.expensetracker.repository;

import com.expensetracker.entity.PayeeCategoryCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayeeCategoryCacheRepository extends JpaRepository<PayeeCategoryCache, Long> {

    Optional<PayeeCategoryCache> findByPayeeNormalized(String payeeNormalized);
}
