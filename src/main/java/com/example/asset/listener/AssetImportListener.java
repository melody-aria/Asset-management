package com.example.asset.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.example.asset.dto.AssetImportDto;
import com.example.asset.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class AssetImportListener implements ReadListener<AssetImportDto> {

    private static final int BATCH_COUNT = 100;
    private List<AssetImportDto> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    
    private final AssetService assetService;

    public AssetImportListener(AssetService assetService) {
        this.assetService = assetService;
    }

    @Override
    public void invoke(AssetImportDto data, AnalysisContext context) {
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        log.info("All data parsed. Total processed.");
    }

    private void saveData() {
        log.info("{} entries to save.", cachedDataList.size());
        assetService.batchImport(cachedDataList);
        log.info("Saved successfully.");
    }
}