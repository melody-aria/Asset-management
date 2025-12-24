package com.example.asset.controller;

import com.alibaba.excel.EasyExcel;
import com.example.asset.entity.Asset;
import com.example.asset.dto.AssetImportDto;
import com.example.asset.listener.AssetImportListener;
import com.example.asset.service.AssetService;
import com.example.asset.service.LocationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.example.asset.dto.AssetSearchDto;

import com.example.asset.service.ManagerService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Controller
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final LocationService locationService;
    private final ManagerService managerService;

    @PostMapping("/delete/batch")
    public String deleteBatch(@RequestParam("ids") List<Long> ids) {
        assetService.deleteBatch(ids);
        return "redirect:/assets";
    }

    @GetMapping
    public String list(@ModelAttribute AssetSearchDto searchDto, 
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.ASC, "id"));
        Page<Asset> assetPage = assetService.search(searchDto, pageable);
        
        model.addAttribute("assets", assetPage.getContent());
        model.addAttribute("page", assetPage);
        model.addAttribute("filterOptions", assetService.getFilterOptions());
        model.addAttribute("searchDto", searchDto);
        
        model.addAttribute("locations", locationService.findAll());
        model.addAttribute("managers", managerService.findAll());
        model.addAttribute("module", "assets");
        return "assets";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Asset asset) {
        assetService.create(asset);
        return "redirect:/assets";
    }

    @PostMapping("/import")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), new com.example.asset.listener.AssetMetaListener(assetService))
                .sheet()
                .headRowNumber(0)
                .doRead();
        return "redirect:/assets";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        assetService.delete(id);
        return "redirect:/assets";
    }

    @PostMapping("/update")
    public String update(@RequestParam("id") Long id, @ModelAttribute Asset asset) {
        assetService.update(id, asset);
        return "redirect:/assets";
    }
    @PostMapping("/transfer")
    public String transfer(@RequestParam("assetId") Long assetId, 
                           @RequestParam("targetLocationId") Long targetLocationId,
                           @RequestParam(value = "targetManagerName", required = false) String targetManagerName) {
        assetService.transferAsset(assetId, targetLocationId, targetManagerName);
        return "redirect:/assets";
    }
}