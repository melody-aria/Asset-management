package com.example.asset.service;

import com.example.asset.dto.AssetDiffDto;
import com.example.asset.entity.Asset;
import com.example.asset.entity.AssetSnapshot;
import com.example.asset.entity.AssetSnapshotItem;
import com.example.asset.repository.AssetRepository;
import com.example.asset.repository.AssetSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AssetRepository assetRepository;
    private final AssetSnapshotRepository snapshotRepository;
    private final OperationLogService logService;

    public List<Integer> getAvailableCreationYears() {
        return assetRepository.findDistinctCreationYears();
    }

    @Transactional
    public AssetSnapshot createSnapshot(String name, LocalDateTime createdTime, Integer filterYear) {
        List<Asset> currentAssets;
        if (filterYear != null) {
            // Using Between logic to be safer with dates
            LocalDateTime startOfYear = LocalDateTime.of(filterYear, 1, 1, 0, 0, 0);
            LocalDateTime endOfYear = LocalDateTime.of(filterYear, 12, 31, 23, 59, 59);
            
            currentAssets = assetRepository.findAll((root, query, cb) -> 
                cb.between(root.get("createdAt"), startOfYear, endOfYear)
            );
        } else {
            currentAssets = assetRepository.findAll();
        }
        
        AssetSnapshot snapshot = new AssetSnapshot();
        snapshot.setName(name);
        snapshot.setCreatedTime(createdTime != null ? createdTime : LocalDateTime.now());
        snapshot.setAssetCount(currentAssets.size());
        
        List<AssetSnapshotItem> items = currentAssets.stream().map(asset -> {
            AssetSnapshotItem item = new AssetSnapshotItem();
            item.setSnapshot(snapshot);
            
            item.setDataSource(asset.getDataSource());
            item.setReportingUnit(asset.getReportingUnit());
            item.setSequenceNumber(asset.getSequenceNumber());
            item.setName(asset.getName());
            item.setCategory(asset.getCategory());
            item.setModel(asset.getModel());
            item.setHasAssetNumber(asset.getHasAssetNumber());
            item.setAssetNumber(asset.getAssetNumber());
            item.setStatus(asset.getStatus());
            item.setPurchaseDate(asset.getPurchaseDate());
            item.setAcquisitionSource(asset.getAcquisitionSource());
            item.setQuantity(asset.getQuantity());
            item.setOriginalValue(asset.getOriginalValue());
            item.setManagementDepartment(asset.getManagementDepartment());
            item.setLocationName(asset.getLocation() != null ? asset.getLocation().getName() : null);
            item.setManager(asset.getManager());
            item.setRemark(asset.getRemark());
            
            return item;
        }).collect(Collectors.toList());

        snapshot.setItems(items);
        return snapshotRepository.save(snapshot);
    }
    
    public List<AssetSnapshot> findAllSnapshots() {
        return snapshotRepository.findAll(Sort.by(Sort.Direction.DESC, "createdTime"));
    }

    @Transactional
    public void deleteSnapshot(Long id) {
        snapshotRepository.findById(id).ifPresent(s -> {
            logService.log("盘点管理", "删除", "删除快照: " + s.getName());
            snapshotRepository.delete(s);
        });
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<AssetSnapshot> snapshots = snapshotRepository.findAllById(ids);
        if (!snapshots.isEmpty()) {
            StringBuilder content = new StringBuilder("批量删除快照: ");
            for (AssetSnapshot s : snapshots) {
                content.append(s.getName()).append(", ");
            }
            if (content.length() > 2) {
                content.setLength(content.length() - 2);
            }
            logService.log("盘点管理", "批量删除", content.toString());
            snapshotRepository.deleteAll(snapshots);
        }
    }

    @Transactional
    public void updateSnapshotName(Long id, String newName) {
        AssetSnapshot snapshot = snapshotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Snapshot not found"));
        snapshot.setName(newName);
        snapshotRepository.save(snapshot);
    }

    @Transactional
    public void updateSnapshot(Long id, String newName, LocalDateTime newTime) {
        AssetSnapshot snapshot = snapshotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Snapshot not found"));
        snapshot.setName(newName);
        if (newTime != null) {
            snapshot.setCreatedTime(newTime);
        }
        snapshotRepository.save(snapshot);
    }

    public List<AssetDiffDto> compareSnapshots(Long oldSnapshotId, Long newSnapshotId) {
        AssetSnapshot oldSnapshot = snapshotRepository.findById(oldSnapshotId)
                .orElseThrow(() -> new RuntimeException("Old snapshot not found"));
        AssetSnapshot newSnapshot = snapshotRepository.findById(newSnapshotId)
                .orElseThrow(() -> new RuntimeException("New snapshot not found"));

        // Helper to generate unique key
        Function<AssetSnapshotItem, String> keyGenerator = item -> {
            if (StringUtils.hasText(item.getAssetNumber())) {
                return "NO:" + item.getAssetNumber();
            } else {
                return "NAME:" + item.getName(); // Fallback to Name if no number
            }
        };

        Map<String, AssetSnapshotItem> oldMap = oldSnapshot.getItems().stream()
                .collect(Collectors.toMap(keyGenerator, Function.identity(), (v1, v2) -> v1));
        
        Map<String, AssetSnapshotItem> newMap = newSnapshot.getItems().stream()
                .collect(Collectors.toMap(keyGenerator, Function.identity(), (v1, v2) -> v1));

        List<AssetDiffDto> diffs = new ArrayList<>();

        // Find Added (in New but not in Old)
        for (AssetSnapshotItem newItem : newSnapshot.getItems()) {
            String key = keyGenerator.apply(newItem);
            if (!oldMap.containsKey(key)) {
                diffs.add(new AssetDiffDto(
                        "新增",
                        newItem.getName(),
                        newItem.getCategory(),
                        newItem.getModel(),
                        newItem.getHasAssetNumber(),
                        newItem.getAssetNumber(),
                        newItem.getStatus(),
                        newItem.getPurchaseDate() != null ? newItem.getPurchaseDate().toString() : "",
                        newItem.getAcquisitionSource(),
                        newItem.getQuantity(),
                        newItem.getOriginalValue(),
                        newItem.getManagementDepartment(),
                        newItem.getLocationName(),
                        newItem.getManager(),
                        newItem.getRemark()
                ));
            }
        }

        // Find Missing (in Old but not in New)
        for (AssetSnapshotItem oldItem : oldSnapshot.getItems()) {
            String key = keyGenerator.apply(oldItem);
            if (!newMap.containsKey(key)) {
                diffs.add(new AssetDiffDto(
                        "丢失",
                        oldItem.getName(),
                        oldItem.getCategory(),
                        oldItem.getModel(),
                        oldItem.getHasAssetNumber(),
                        oldItem.getAssetNumber(),
                        oldItem.getStatus(),
                        oldItem.getPurchaseDate() != null ? oldItem.getPurchaseDate().toString() : "",
                        oldItem.getAcquisitionSource(),
                        oldItem.getQuantity(),
                        oldItem.getOriginalValue(),
                        oldItem.getManagementDepartment(),
                        oldItem.getLocationName(),
                        oldItem.getManager(),
                        oldItem.getRemark()
                ));
            }
        }

        return diffs;
    }
}
