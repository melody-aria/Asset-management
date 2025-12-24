package com.example.asset.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String module;      // 模块: 资产管理, 部门管理, 等
    private String operationType; // 类型: 删除, 批量删除
    
    @Column(columnDefinition = "TEXT")
    private String content;     // 详情: 删除了资产 xxx (ID: 1)
    
    private String operator;    // 操作人
    
    private LocalDateTime operateTime; // 时间

    @PrePersist
    public void prePersist() {
        if (operateTime == null) {
            operateTime = LocalDateTime.now();
        }
        if (operator == null) {
            operator = "Admin"; // 默认操作人
        }
    }
}
