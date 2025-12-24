package com.example.asset.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class AssetTransferLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long assetId;
    private String assetName;
    private String assetNumber;

    private String fromLocationName;
    private String toLocationName;

    private LocalDateTime transferTime;
    private String operator; // Could be User ID, keeping simple for now
}