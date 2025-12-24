package com.example.asset.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class AssetSnapshotItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id")
    private AssetSnapshot snapshot;

    // 复制自 Asset 的字段
    private String dataSource;
    private String reportingUnit;
    private Integer sequenceNumber;
    private String name;
    private String category;
    private String model;
    private String hasAssetNumber;
    private String assetNumber;
    private String status;
    private LocalDate purchaseDate;
    private String acquisitionSource;
    private Integer quantity;
    private BigDecimal originalValue;
    private String managementDepartment;
    private String locationName; // 对应 Asset.location.name
    private String manager;
    private String remark;
}