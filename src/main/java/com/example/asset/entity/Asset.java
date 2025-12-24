package com.example.asset.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 数据来源
    private String dataSource;
    
    // 填报单位
    private String reportingUnit;
    
    // 序号
    private Integer sequenceNumber;

    // 资产名称
    private String name;
    
    // 资产类别
    private String category;
    
    // 规格型号
    private String model;
    
    // 有无资产编号 (有/无)
    private String hasAssetNumber;

    // 资产编号 (允许为空，因为"有无资产编号"可能为无)
    private String assetNumber;

    // 使用状态
    private String status;
    
    // 取得时间
    private LocalDate purchaseDate;
    
    // 取得来源
    private String acquisitionSource;
    
    // 数量
    private Integer quantity;
    
    // 原值
    private BigDecimal originalValue;

    // 管理使用单位（部门）
    private String managementDepartment;

    // 资产放置位置 (关联 Location 表，为了保持之前的逻辑，同时适配新需求)
    // 导入时，将 Excel 的"资产放置位置"映射到这里
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;
    
    // 管理（使用人）
    private String manager;
    
    // 备注
    private String remark;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}