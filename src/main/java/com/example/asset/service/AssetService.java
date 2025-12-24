package com.example.asset.service;

import com.example.asset.dto.AssetImportDto;
import com.example.asset.dto.AssetSearchDto;
import com.example.asset.entity.Asset;
import com.example.asset.entity.AssetStatus;
import com.example.asset.entity.AssetTransferLog;
import com.example.asset.entity.Location;
import com.example.asset.repository.AssetRepository;
import com.example.asset.repository.AssetTransferLogRepository;
import com.example.asset.repository.LocationRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;
    private final LocationRepository locationRepository;
    private final AssetTransferLogRepository transferLogRepository;
    private final LocationService locationService;
    private final OperationLogService logService;

    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    public List<Asset> search(String keyword) {
        return assetRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Page<Asset> search(AssetSearchDto searchDto, Pageable pageable) {
        return assetRepository.findAll((Specification<Asset>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(searchDto.getKeyword())) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + searchDto.getKeyword().toLowerCase() + "%"));
            }

            if (StringUtils.hasText(searchDto.getCategory())) {
                predicates.add(cb.equal(root.get("category"), searchDto.getCategory()));
            }
            if (StringUtils.hasText(searchDto.getHasAssetNumber())) {
                predicates.add(cb.equal(root.get("hasAssetNumber"), searchDto.getHasAssetNumber()));
            }
            if (StringUtils.hasText(searchDto.getStatus())) {
                predicates.add(cb.equal(root.get("status"), searchDto.getStatus()));
            }
            if (StringUtils.hasText(searchDto.getPurchaseDate())) {
                 try {
                    // Assuming searchDto.getPurchaseDate() is in yyyy-MM-dd format
                    LocalDate date = LocalDate.parse(searchDto.getPurchaseDate());
                    predicates.add(cb.equal(root.get("purchaseDate"), date));
                } catch (Exception e) {
                    // Ignore invalid date
                }
            }
            if (StringUtils.hasText(searchDto.getAcquisitionSource())) {
                predicates.add(cb.equal(root.get("acquisitionSource"), searchDto.getAcquisitionSource()));
            }
            if (StringUtils.hasText(searchDto.getManagementDepartment())) {
                predicates.add(cb.equal(root.get("managementDepartment"), searchDto.getManagementDepartment()));
            }
            if (StringUtils.hasText(searchDto.getManager())) {
                predicates.add(cb.equal(root.get("manager"), searchDto.getManager()));
            }
            if (StringUtils.hasText(searchDto.getLocationName())) {
                // Join with Location
                predicates.add(cb.equal(root.get("location").get("name"), searchDto.getLocationName()));
            }
            if (searchDto.getCreationYear() != null) {
                predicates.add(cb.equal(cb.function("YEAR", Integer.class, root.get("createdAt")), searchDto.getCreationYear()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    public Map<String, List<Object>> getFilterOptions() {
        List<Asset> allAssets = assetRepository.findAll();
        Map<String, List<Object>> options = new HashMap<>();

        options.put("categories", allAssets.stream().map(Asset::getCategory).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        options.put("statuses", allAssets.stream().map(Asset::getStatus).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        options.put("sources", allAssets.stream().map(Asset::getAcquisitionSource).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        options.put("departments", allAssets.stream().map(Asset::getManagementDepartment).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        options.put("managers", allAssets.stream().map(Asset::getManager).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        options.put("locations", allAssets.stream().map(a -> a.getLocation() != null ? a.getLocation().getName() : null).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        options.put("hasAssetNumbers", allAssets.stream().map(Asset::getHasAssetNumber).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        // For date, maybe just years? Or full dates? Let's do full dates for now.
        options.put("purchaseDates", allAssets.stream().map(a -> a.getPurchaseDate() != null ? a.getPurchaseDate().toString() : null).filter(StringUtils::hasText).distinct().sorted().collect(Collectors.toList()));
        
        // Creation Years (Integer)
        options.put("creationYears", assetRepository.findDistinctCreationYears().stream().collect(Collectors.toList()));

        return options;
    }
    
    public Page<Asset> findAll(Pageable pageable) {
        return assetRepository.findAll(pageable);
    }

    public Asset findById(Long id) {
        return assetRepository.findById(id).orElseThrow(() -> new RuntimeException("Asset not found"));
    }

    @Transactional
    public void transferAsset(Long assetId, Long targetLocationId, String targetManagerName) {
        Asset asset = findById(assetId);
        Location oldLocation = asset.getLocation();
        String oldManager = asset.getManager();
        
        Location newLocation = locationRepository.findById(targetLocationId)
                .orElseThrow(() -> new RuntimeException("Target location not found"));

        // Check if anything changed
        boolean locationChanged = oldLocation == null || !oldLocation.getId().equals(targetLocationId);
        boolean managerChanged = targetManagerName != null && !targetManagerName.equals(oldManager);
        
        if (!locationChanged && !managerChanged) {
            return;
        }

        // Create Log
        AssetTransferLog logEntry = new AssetTransferLog();
        logEntry.setAssetId(asset.getId());
        logEntry.setAssetName(asset.getName());
        logEntry.setAssetNumber(asset.getAssetNumber());
        logEntry.setFromLocationName(oldLocation != null ? oldLocation.getName() : "N/A");
        logEntry.setToLocationName(newLocation.getName());
        // We might want to log manager change too, but AssetTransferLog only has location fields currently.
        // For now, we focus on transferring the state.
        logEntry.setTransferTime(LocalDateTime.now());
        logEntry.setOperator("Admin"); 

        transferLogRepository.save(logEntry);

        // Update Asset
        asset.setLocation(newLocation);
        if (targetManagerName != null) {
            asset.setManager(targetManagerName);
        }
        assetRepository.save(asset);
    }

    @Transactional
    public void create(Asset asset) {
        // 如果有资产编号，检查唯一性
        if (StringUtils.hasText(asset.getAssetNumber())) {
            assetRepository.findByAssetNumber(asset.getAssetNumber()).ifPresent(a -> {
                throw new RuntimeException("资产编号已存在: " + asset.getAssetNumber());
            });
        }
        assetRepository.save(asset);
    }

    @Transactional
    public void delete(Long id) {
        assetRepository.findById(id).ifPresent(asset -> {
            logService.log("资产管理", "删除", "删除资产: " + asset.getName() + " (编号: " + asset.getAssetNumber() + ")");
            assetRepository.delete(asset);
        });
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<Asset> assets = assetRepository.findAllById(ids);
        if (!assets.isEmpty()) {
            StringBuilder content = new StringBuilder("批量删除资产: ");
            for (Asset asset : assets) {
                content.append(asset.getName()).append("(编号:").append(asset.getAssetNumber()).append("), ");
            }
            // Remove last comma
            if (content.length() > 2) {
                content.setLength(content.length() - 2);
            }
            // Truncate if too long (OperationLog content is TEXT, so plenty of space, but good practice)
            
            logService.log("资产管理", "批量删除", content.toString());
            assetRepository.deleteAll(assets);
        }
    }

    @Transactional
    public void update(Long id, Asset updatedAsset) {
        Asset asset = findById(id);
        asset.setName(updatedAsset.getName());
        asset.setCategory(updatedAsset.getCategory());
        asset.setModel(updatedAsset.getModel());
        asset.setHasAssetNumber(updatedAsset.getHasAssetNumber());
        asset.setAssetNumber(updatedAsset.getAssetNumber());
        asset.setStatus(updatedAsset.getStatus());
        asset.setPurchaseDate(updatedAsset.getPurchaseDate());
        asset.setAcquisitionSource(updatedAsset.getAcquisitionSource());
        asset.setQuantity(updatedAsset.getQuantity());
        asset.setOriginalValue(updatedAsset.getOriginalValue());
        asset.setManagementDepartment(updatedAsset.getManagementDepartment());
        asset.setManager(updatedAsset.getManager());
        asset.setRemark(updatedAsset.getRemark());
        asset.setDataSource(updatedAsset.getDataSource());
        asset.setReportingUnit(updatedAsset.getReportingUnit());
        
        // 注意：位置 (Location) 的更新比较特殊，如果前端传的是 locationName，需要在这里处理
        // 或者前端传 location.id。
        // 为了简单，我们假设前端只改了文本属性。如果改了位置，可能需要 transferAsset 逻辑。
        // 但既然是"修改"，我们这里允许直接改关联。
        // 不过 Asset 实体里没有 locationName 字段，只有 Location 对象。
        // 如果要支持修改位置，最好通过 transferAsset。
        // 但用户说"修改目前显示的字段"，其中包含"资产放置位置"。
        // 这里的"资产放置位置"显示的是 Location.name。
        // 如果我们允许在这里改，意味着要么选一个新的 Location，要么改当前 Location 的名字（这不对）。
        // 暂时我们只更新 Asset 本身的字段。如果需要改位置，应该用"转移"功能，或者在这里提供下拉框选 Location。
        
        assetRepository.save(asset);
    }
    private final ManagerService managerService;

    @Transactional
    public void batchImport(List<AssetImportDto> importList) {
        for (AssetImportDto dto : importList) {
            Asset asset = null;
            // 只有当明确有资产编号时，才尝试去数据库查找是否存在并更新
            if (StringUtils.hasText(dto.getAssetNumber())) {
                asset = assetRepository.findByAssetNumber(dto.getAssetNumber())
                        .orElse(new Asset());
            } else {
                asset = new Asset();
            }

            // 映射所有新字段
            asset.setDataSource(dto.getDataSource());
            asset.setReportingUnit(dto.getReportingUnit());
            asset.setSequenceNumber(dto.getSequenceNumber());
            asset.setName(dto.getName());
            asset.setCategory(dto.getCategory());
            asset.setModel(dto.getModel());
            asset.setHasAssetNumber(dto.getHasAssetNumber());
            asset.setAssetNumber(dto.getAssetNumber());
            asset.setStatus(dto.getStatus()); // 现在是 String 类型
            asset.setAcquisitionSource(dto.getAcquisitionSource());
            asset.setQuantity(dto.getQuantity());
            asset.setOriginalValue(dto.getOriginalValue());
            asset.setManagementDepartment(dto.getManagementDepartment());
            asset.setManager(dto.getManager());
            asset.setRemark(dto.getRemark());

            // Parse Date
            if (StringUtils.hasText(dto.getPurchaseDate())) {
                try {
                    // 简单处理日期格式，例如 2023/01/01 或 2023-01-01
                    String dateStr = dto.getPurchaseDate().replace("/", "-").trim();
                    if (dateStr.contains(" ")) {
                        dateStr = dateStr.split(" ")[0];
                    }
                    asset.setPurchaseDate(LocalDate.parse(dateStr));
                } catch (Exception e) {
                    log.warn("Invalid date format for asset {}: {}", dto.getName(), dto.getPurchaseDate());
                }
            }

            // Location (资产放置位置)
            // 逻辑更新：优先使用 "管理使用单位（部门）" 作为位置
            // 如果 "管理使用单位（部门）" 为空，才使用 "资产放置位置"
            String locationName = dto.getManagementDepartment();
            if (!StringUtils.hasText(locationName)) {
                locationName = dto.getLocationName();
            }

            if (StringUtils.hasText(locationName)) {
                Location loc = locationService.findByNameOrCreate(locationName);
                asset.setLocation(loc);
            }
            
            // Manager (管理员) - 自动创建或更新 Manager 表
            if (StringUtils.hasText(dto.getManager())) {
                managerService.findByNameOrCreate(dto.getManager());
            }

            assetRepository.save(asset);
        }
    }
}
