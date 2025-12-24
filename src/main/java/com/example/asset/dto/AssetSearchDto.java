package com.example.asset.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AssetSearchDto {
    private String keyword; // General search (name)
    
    // Filter fields
    private String category;
    private String hasAssetNumber;
    private String status;
    private String purchaseDate; // Using String to simplify date matching (or LocalDate)
    private String acquisitionSource;
    private String managementDepartment;
    private String locationName; // Filter by location name
    private String manager;
    private Integer creationYear; // Filter by creation year (created_at)
}
