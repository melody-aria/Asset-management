package com.example.asset.repository;

import com.example.asset.entity.AssetSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetSnapshotRepository extends JpaRepository<AssetSnapshot, Long> {
}