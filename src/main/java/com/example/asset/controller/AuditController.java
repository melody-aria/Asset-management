package com.example.asset.controller;

import com.example.asset.dto.AssetDiffDto;
import com.example.asset.service.AuditService;
import com.example.asset.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("snapshots", auditService.findAllSnapshots());
        model.addAttribute("creationYears", auditService.getAvailableCreationYears());
        model.addAttribute("module", "audit");
        return "audit";
    }

    @PostMapping("/snapshot")
    public String createSnapshot(@RequestParam("name") String name, 
                                 @RequestParam(value = "createdTime", required = false) String createdTimeStr,
                                 @RequestParam(value = "filterYear", required = false) Integer filterYear) {
        LocalDateTime createdTime = null;
        if (createdTimeStr != null && !createdTimeStr.isEmpty()) {
            createdTime = LocalDateTime.parse(createdTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        auditService.createSnapshot(name, createdTime, filterYear);
        return "redirect:/audit";
    }

    @GetMapping("/compare")
    public String compare(@RequestParam("oldSnapshotId") Long oldSnapshotId,
                          @RequestParam("newSnapshotId") Long newSnapshotId,
                          Model model) {
        List<AssetDiffDto> diffs = auditService.compareSnapshots(oldSnapshotId, newSnapshotId);
        model.addAttribute("diffs", diffs);
        model.addAttribute("snapshots", auditService.findAllSnapshots());
        model.addAttribute("creationYears", auditService.getAvailableCreationYears());
        model.addAttribute("oldSnapshotId", oldSnapshotId);
        model.addAttribute("newSnapshotId", newSnapshotId);
        model.addAttribute("module", "audit");
        return "audit";
    }

    @PostMapping("/snapshot/delete/{id}")
    public String deleteSnapshot(@PathVariable Long id) {
        auditService.deleteSnapshot(id);
        return "redirect:/audit";
    }

    @PostMapping("/snapshot/delete/batch")
    public String deleteBatch(@RequestParam("ids") List<Long> ids) {
        auditService.deleteBatch(ids);
        return "redirect:/audit";
    }

    @PostMapping("/snapshot/update")
    public String updateSnapshot(@RequestParam("id") Long id, 
                                 @RequestParam("name") String name,
                                 @RequestParam(value = "createdTime", required = false) String createdTimeStr) {
        LocalDateTime createdTime = null;
        if (createdTimeStr != null && !createdTimeStr.isEmpty()) {
            createdTime = LocalDateTime.parse(createdTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        auditService.updateSnapshot(id, name, createdTime);
        return "redirect:/audit";
    }
    @GetMapping("/export")
    public void export(@RequestParam("oldSnapshotId") Long oldSnapshotId,
                       @RequestParam("newSnapshotId") Long newSnapshotId,
                       HttpServletResponse response) throws IOException {
        List<AssetDiffDto> diffs = auditService.compareSnapshots(oldSnapshotId, newSnapshotId);
        ExcelUtil.writeExcel(response, diffs, AssetDiffDto.class, "Audit_Comparison_Report");
    }
}