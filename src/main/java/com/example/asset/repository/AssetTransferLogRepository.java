package com.example.asset.repository;

import com.example.asset.entity.AssetTransferLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssetTransferLogRepository extends JpaRepository<AssetTransferLog, Long> {
    List<AssetTransferLog> findByAssetIdOrderByTransferTimeDesc(Long assetId);
}