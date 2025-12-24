package com.example.asset.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AssetImportDto {
    @ExcelProperty("数据来源")
    private String dataSource;

    @ExcelProperty("填报单位")
    private String reportingUnit;

    @ExcelProperty("序号")
    private Integer sequenceNumber;

    @ExcelProperty("资产名称")
    private String name;

    @ExcelProperty("资产类别")
    private String category;

    @ExcelProperty("规格型号")
    private String model;

    @ExcelProperty("有无资产编号")
    private String hasAssetNumber;

    @ExcelProperty("资产编号")
    private String assetNumber;

    @ExcelProperty("使用状态")
    private String status;

    @ExcelProperty("取得时间")
    private String purchaseDate;

    @ExcelProperty("取得来源")
    private String acquisitionSource;

    @ExcelProperty("数量")
    private Integer quantity;

    @ExcelProperty("原值")
    private BigDecimal originalValue;

    @ExcelProperty("管理使用单位（部门）")
    private String managementDepartment;

    @ExcelProperty("资产放置位置")
    private String locationName;

    @ExcelProperty("管理（使用人）")
    private String manager;

    @ExcelProperty("备注")
    private String remark;
}