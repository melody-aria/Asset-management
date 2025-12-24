package com.example.asset.repository;

import com.example.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {
    Optional<Asset> findByAssetNumber(String assetNumber);
    
    List<Asset> findByNameContainingIgnoreCase(String name);
    
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT YEAR(a.createdAt) FROM Asset a ORDER BY YEAR(a.createdAt) DESC")
    List<Integer> findDistinctCreationYears();
}
