package com.expensetracker.repository;

import com.expensetracker.entity.MccCategoryMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MccCategoryMappingRepository extends JpaRepository<MccCategoryMapping, String> {

    Optional<MccCategoryMapping> findByMccCode(String mccCode);
}
