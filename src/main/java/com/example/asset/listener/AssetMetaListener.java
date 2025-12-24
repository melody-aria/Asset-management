package com.example.asset.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.example.asset.dto.AssetImportDto;
import com.example.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AssetMetaListener extends AnalysisEventListener<Map<Integer, String>> {

    private final AssetService assetService;
    private String dataSource;
    private String reportingUnit;
    
    // Store header column index mapping
    private Map<String, Integer> headerMap = new HashMap<>();

    private static final int BATCH_COUNT = 100;
    private List<AssetImportDto> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);

    public AssetMetaListener(AssetService assetService) {
        this.assetService = assetService;
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        int rowIndex = context.readRowHolder().getRowIndex();

        if (rowIndex == 0) {
            this.dataSource = data.get(0); 
            log.info("Parsed DataSource: {}", this.dataSource);
            return;
        }

        if (rowIndex == 1) {
            String content = data.get(0);
            if (content != null && content.contains("填报单位")) {
                 this.reportingUnit = content.replace("填报单位：", "").replace("填报单位:", "").trim();
            } else {
                 this.reportingUnit = content;
            }
            log.info("Parsed ReportingUnit: {}", this.reportingUnit);
            return;
        }

        if (rowIndex == 2) {
            // Parse Header
            for (Map.Entry<Integer, String> entry : data.entrySet()) {
                if (entry.getValue() != null) {
                    headerMap.put(entry.getValue().trim(), entry.getKey());
                }
            }
            log.info("Parsed Headers: {}", headerMap);
            return;
        }

        // 第四行及以后：数据行
        try {
            AssetImportDto dto = new AssetImportDto();
            dto.setDataSource(this.dataSource);
            dto.setReportingUnit(this.reportingUnit);
            
            dto.setSequenceNumber(getIntegerVal(data, "序号"));
            dto.setName(getStringVal(data, "资产名称"));
            dto.setCategory(getStringVal(data, "资产类别"));
            dto.setModel(getStringVal(data, "规格型号"));
            dto.setHasAssetNumber(getStringVal(data, "有无资产编号"));
            dto.setAssetNumber(getStringVal(data, "资产编号"));
            dto.setStatus(getStringVal(data, "使用状态"));
            dto.setPurchaseDate(getStringVal(data, "取得时间"));
            dto.setAcquisitionSource(getStringVal(data, "取得来源"));
            dto.setQuantity(getIntegerVal(data, "数量"));
            dto.setOriginalValue(getBigDecimalVal(data, "原值"));
            dto.setManagementDepartment(getStringVal(data, "管理使用单位（部门）"));
            dto.setLocationName(getStringVal(data, "资产放置位置"));
            dto.setManager(getStringVal(data, "管理（使用人）"));
            dto.setRemark(getStringVal(data, "备注"));
            
            cachedDataList.add(dto);
            if (cachedDataList.size() >= BATCH_COUNT) {
                saveData();
                cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
            }
        } catch (Exception e) {
            log.error("Error parsing row {}", rowIndex, e);
        }
    }
    
    private String getStringVal(Map<Integer, String> data, String headerName) {
        Integer index = headerMap.get(headerName);
        if (index != null) {
            return data.get(index);
        }
        return null;
    }
    
    private Integer getIntegerVal(Map<Integer, String> data, String headerName) {
        Integer index = headerMap.get(headerName);
        if (index != null) {
            String val = data.get(index);
            if (val != null) {
                try {
                    return Integer.parseInt(val.trim());
                } catch (Exception e) {}
            }
        }
        return null;
    }
    
    private BigDecimal getBigDecimalVal(Map<Integer, String> data, String headerName) {
        Integer index = headerMap.get(headerName);
        if (index != null) {
            String val = data.get(index);
            if (val != null) {
                try {
                    return new BigDecimal(val.trim());
                } catch (Exception e) {}
            }
        }
        return null;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
    }

    private void saveData() {
        if (!cachedDataList.isEmpty()) {
            assetService.batchImport(cachedDataList);
        }
    }
}