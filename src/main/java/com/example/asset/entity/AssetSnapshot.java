package com.example.asset.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class AssetSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // e.g., "2024 Year End Audit"
    private LocalDateTime createdTime;
    
    private Integer assetCount; // Number of assets in this snapshot

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssetSnapshotItem> items = new ArrayList<>();
}